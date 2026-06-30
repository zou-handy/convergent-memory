package com.convergentmemory.agent;

import com.convergentmemory.dto.ConvergeDraft;
import com.convergentmemory.entity.MemoryEntry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
@Slf4j
public class ConvergerAgent {

    private String getDashscopeKey() {
        return System.getenv("DASHSCOPE_API_KEY");
    }

    public ConvergeDraft ruleBasedConverge(List<MemoryEntry> inboxEntries) {
        ConvergeDraft draft = new ConvergeDraft();
        draft.setMode("rule-based");
        draft.setInputCount(inboxEntries.size());

        if (inboxEntries.isEmpty()) {
            draft.setSummary("inbox 为空,无可收敛");
            return draft;
        }

        int n = inboxEntries.size();
        int[] parent = new int[n];
        for (int i = 0; i < n; i++) parent[i] = i;

        for (int i = 0; i < n; i++) {
            Set<String> cuesI = parseCues(inboxEntries.get(i).getCueTags());
            for (int j = i + 1; j < n; j++) {
                Set<String> cuesJ = parseCues(inboxEntries.get(j).getCueTags());
                if (overlap(cuesI, cuesJ) >= 1) {
                    union(parent, i, j);
                }
            }
        }

        Map<Integer, List<Integer>> groups = new LinkedHashMap<>();
        for (int i = 0; i < n; i++) {
            groups.computeIfAbsent(find(parent, i), k -> new ArrayList<>()).add(i);
        }

        for (Map.Entry<Integer, List<Integer>> g : groups.entrySet()) {
            List<Integer> indices = g.getValue();
            ConvergeDraft.Cluster cluster = new ConvergeDraft.Cluster();

            Set<String> mergedCues = new LinkedHashSet<>();
            List<Long> ids = new ArrayList<>();
            StringBuilder content = new StringBuilder();

            for (int idx : indices) {
                MemoryEntry e = inboxEntries.get(idx);
                ids.add(e.getId());
                mergedCues.addAll(parseCues(e.getCueTags()));
                content.append("- 来源 #").append(e.getId()).append(" ").append(e.getTitle()).append("\n");
                if (e.getSummary() != null) content.append("  - ").append(e.getSummary()).append("\n");
            }

            String topCue = mergedCues.isEmpty() ? "未分类" : mergedCues.iterator().next();
            String suggestedTitle = topCue + "聚合";
            String slug = slugify(suggestedTitle);
            String category = indices.size() >= 3 ? "CORE" : "CONTEXT";

            cluster.setSuggestedTitle(suggestedTitle);
            cluster.setSuggestedCategory(category);
            cluster.setSuggestedFilePath(category.toLowerCase() + "/" + slug + ".md");
            cluster.setSourceIds(ids);
            cluster.setSourceCueTags(new ArrayList<>(mergedCues));
            cluster.setDraftContent(buildDraftContent(suggestedTitle, mergedCues, content.toString(), indices.size()));
            cluster.setConfidence(Math.min(1.0, indices.size() * 0.25));

            draft.getClusters().add(cluster);
        }

        draft.setSummary(String.format("规则版收敛完毕:%d 条 inbox -> %d 个 cluster",
                n, draft.getClusters().size()));
        log.info("ruleBasedConverge: {} entries -> {} clusters", n, draft.getClusters().size());
        return draft;
    }

    public ConvergeDraft llmConverge(List<MemoryEntry> inboxEntries) {
        ConvergeDraft draft = new ConvergeDraft();
        draft.setMode("llm");
        draft.setInputCount(inboxEntries.size());

        String key = getDashscopeKey();
        if (key == null || key.isBlank()) {
            draft.setSummary("DASHSCOPE_API_KEY 未配置,跳过 LLM 收敛");
            log.warn("llmConverge skipped: no DASHSCOPE_API_KEY");
            return draft;
        }

        try {
            io.agentscope.core.model.OpenAIChatModel model = io.agentscope.core.model.OpenAIChatModel.builder()
                    .apiKey(key)
                    .modelName("qwen-max")
                    .baseUrl("https://dashscope.aliyuncs.com/compatible-mode/v1")
                    .build();

            io.agentscope.harness.agent.HarnessAgent agent = io.agentscope.harness.agent.HarnessAgent.builder()
                    .name("converger")
                    .sysPrompt("你是个人记忆收敛器。读取 inbox 散记 JSON,按主题聚类,每组输出 title/category/summary 草案。返回 JSON 数组。")
                    .model(model)
                    .build();

            String inboxJson = inboxEntries.stream()
                    .map(e -> String.format("{\"id\":%d,\"title\":\"%s\",\"cueTags\":\"%s\",\"summary\":\"%s\"}",
                            e.getId(),
                            escape(e.getTitle()),
                            escape(e.getCueTags() == null ? "" : e.getCueTags()),
                            escape(e.getSummary() == null ? "" : e.getSummary())))
                    .collect(Collectors.joining(",", "[", "]"));

            String prompt = "请收敛以下 " + inboxEntries.size() + " 条 inbox 散记:\n" + inboxJson;
            io.agentscope.core.message.Msg result = agent.call(prompt,
                    io.agentscope.core.agent.RuntimeContext.empty()).block();

            String llmText = result == null ? "" : result.getTextContent();
            log.info("llmConverge raw response length: {}", llmText.length());

            ConvergeDraft.Cluster cluster = new ConvergeDraft.Cluster();
            cluster.setSuggestedTitle("LLM 收敛草案");
            cluster.setSuggestedCategory("CORE");
            cluster.setSuggestedFilePath("core/llm-draft-" + System.currentTimeMillis() + ".md");
            cluster.setSourceIds(inboxEntries.stream().map(MemoryEntry::getId).collect(Collectors.toList()));
            cluster.setDraftContent("# LLM 收敛草案\n\n" + llmText);
            cluster.setConfidence(0.7);
            draft.getClusters().add(cluster);
            draft.setSummary("LLM 收敛完毕:1 个综合草案,源自 " + inboxEntries.size() + " 条");
        } catch (Throwable t) {
            log.error("llmConverge failed", t);
            draft.setSummary("LLM 收敛失败:" + t.getClass().getSimpleName() + " - " + t.getMessage());
        }
        return draft;
    }

    private static Set<String> parseCues(String cueTags) {
        if (cueTags == null || cueTags.isBlank()) return Collections.emptySet();
        return Arrays.stream(cueTags.split(","))
                .map(String::trim).filter(s -> !s.isEmpty()).collect(Collectors.toSet());
    }

    private static int overlap(Set<String> a, Set<String> b) {
        if (a.isEmpty() || b.isEmpty()) return 0;
        int c = 0;
        for (String s : a) if (b.contains(s)) c++;
        return c;
    }

    private static int find(int[] p, int i) {
        while (p[i] != i) { p[i] = p[p[i]]; i = p[i]; }
        return i;
    }

    private static void union(int[] p, int a, int b) {
        int ra = find(p, a), rb = find(p, b);
        if (ra != rb) p[ra] = rb;
    }

    private static String slugify(String s) {
        if (s == null) return "untitled";
        String slug = s.toLowerCase()
                .replaceAll("[\\s_]+", "-")
                .replaceAll("[^a-z0-9\\u4e00-\\u9fa5-]", "")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");
        return slug.isEmpty() ? "untitled" : (slug.length() > 50 ? slug.substring(0, 50) : slug);
    }

    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
    }

    private static String buildDraftContent(String title, Set<String> cues, String body, int memberCount) {
        StringBuilder sb = new StringBuilder();
        sb.append("---\n");
        sb.append("title: ").append(title).append("\n");
        sb.append("converger: rule-based\n");
        sb.append("cues: [").append(String.join(", ", cues)).append("]\n");
        sb.append("members: ").append(memberCount).append("\n");
        sb.append("---\n\n");
        sb.append("# ").append(title).append("\n\n");
        sb.append("> 由规则版 ConvergerAgent 自动凝练,聚类依据为 cueTags 重叠\n\n");
        sb.append("## 来源\n\n").append(body).append("\n");
        sb.append("## 关键线索\n\n");
        for (String c : cues) sb.append("- ").append(c).append("\n");
        return sb.toString();
    }
}
