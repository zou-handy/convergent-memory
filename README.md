# Convergent Memory

**A letter from your last session to your next one.**

> [!IMPORTANT]
> **Project status — August 2026:** the Markdown protocol on this default `master` branch is an experimental version and is no longer actively maintained. The earlier **Java / Spring Boot server implementation was not lost**: its complete source is preserved on the [`main` branch](../../tree/main). That version was successfully deployed on DigitalOcean and demonstrated in a Java class; the original server has expired and its public URL is now offline.

## Repository map

| Branch | Implementation | Current status |
|---|---|---|
| [`master`](../../tree/master) | Local-first Markdown / Claude Code skill | Experimental, currently inactive |
| [`main`](../../tree/main) | Spring Boot multi-user AI memory REST API | Source preserved; former deployment offline |
| [`merge-main-into-master`](../../tree/merge-main-into-master) | Historical integration attempt | Kept only for history |

The two main branches are different implementations of the same idea, not two normal release branches. For portfolio review, the Java project on `main` is the version that provides the clearest evidence of a working server application: 31 Java source files, REST endpoints, Spring Security, JPA/H2, Swagger and an AgentScope-based convergence flow.

The server source remains available, but the original deployment stored its H2 index and Markdown vault under server-local paths such as `/root/convergent-memory/data/` and `/root/memory-vault/`. Unless a DigitalOcean snapshot or separate backup exists, the old runtime data should be treated as unavailable.

---

Convergent Memory is a local-first, human-readable, cross-agent long-term memory protocol. It turns a folder of Markdown files into a living knowledge base that gets better every time an AI agent reads and rewrites it.

No vector database. No API keys. No services. md is the protocol, folder hierarchy is the structure, convergence is the write action.

## Why This Exists

Most agent memory systems are **crutches** — vector databases, embedding pipelines, context compression modules. They exist to compensate for weak models. When the model gets stronger, the crutch becomes waste.

Convergent Memory is **notebook**, not scaffolding. If the model becomes infinitely capable tomorrow, a well-maintained notebook is still useful — you just hand it to the stronger model and it reads it directly. Crutches get discarded; notebooks don't.

## How It Works

```text
Divergent notes (scattered inspirations, drafts, TODOs)
        │
        ▼  Convergence: the model decides what's worth repeated recall
Converged profiles (authoritative, rewritten, ranked by importance)
        │
        ▼  Archival: old reasoning traces preserved, not deleted
Archive (read only when digging up history)
```

**Three moves, not one:**

- **Recall** — the agent reads the converged files directly (`read` replaces `inject`).
- **Convergence** — rewrite to merge and shorten, never just append. Like matrix rank reduction.
- **Archive** — move stable-old conclusions out of the hot path; don't delete them.

## Why It's Different

| | Typical Memory System | Convergent Memory |
|---|---|---|
| Compression | Runtime, by a dedicated sub-agent | Between sessions, by the same conversation model, offline |
| Storage | Vector DB / SQLite / SaaS | Local Markdown files |
| Retrieval | Embedding similarity search | `read` system call |
| Data sovereignty | Handed to a platform | Stays on disk, human-readable |
| The model's job | Process queries | Also the compressor — writes letters to its future self |

> **The core reframe:** context compression moves from "runtime, by a dedicated compression sub-agent" to "between sessions, by the same conversation model, offline." One round's model rewrites what the next round's model should recall — a letter to its future self. The compressor is the conversation model itself; recall is `read`; everything is prepped offline between sessions.

## Getting Started

This section is retained for historical and experimental use. The project is not currently under active maintenance.

### Install

```bash
git clone https://github.com/hd18512614931-cyber/convergent-memory
mkdir -p ~/.claude/skills
cp -r convergent-memory/skills/convergent-memory ~/.claude/skills/
```

Or as a Claude Code plugin:

```text
/plugin marketplace add hd18512614931-cyber/convergent-memory
/plugin install convergent-memory@convergent-memory
```

### Folder Convention

```text
your-memory-vault/
├── (scattered .md)          ← Divergent layer: raw inspirations, drafts
├── profiles/                ← Converged layer: authoritative, rewritten
│   ├── core-profile.md      ← Permanent sub-layer: read every turn
│   └── context/             ← Contextual sub-layer: recalled by topic
└── archive/                 ← Archived traces: read only on deep dives
```

The folder names can evolve — what matters is the three-layer semantics, not the exact names.

### When It Triggers

1. **User says "converge"** — default, safest.
2. **Session ends** — scan divergent notes, precipitate signals.
3. **Scheduled batch** — when enough new material has accumulated.
4. **After complex tasks** — ask whether the result is worth saving.

The trigger is deterministic; what counts as worth keeping is the model's judgment.

## Principles

- **Rewrite over append.** Same topic? Merge, deduplicate, reduce rank. Archive the excess.
- **Extract, don't invent.** Every line in a profile must trace back to something the user actually said or clearly implied.
- **Contradiction = rewrite with a trace.** Preserve how conclusions changed instead of silently replacing history.
- **Self-contained and portable.** Any converged file should make sense to a fresh agent with no session context.
- **Convergence ≠ response.** Saving facts to the vault and answering the current question are separate acts.

## The Test

> "If the model became infinitely strong, would this still be needed?"

Crutches — discard.  
Weapons — get better.  
A notebook — stays. It is the data, not the architecture, that compounds.

## License

MIT — see [LICENSE](LICENSE).

---

*Built for anyone who wants their AI agents to share a memory that outlives any single session, framework, or platform. md is the protocol; everything else is optional.*
