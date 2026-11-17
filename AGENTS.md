# AGENTS.md — joutio-console

Console d'**exploitation Axon**, équivalent open source de l'Axoniq Platform : elle reçoit les
instantanés des agents du SDK, montre l'état des processors, des segments et des DLQ, et relaie
les actions (retry, purge, pause, replay, split/merge) en les **auditant**. Surface REST **admin**
dédiée (exception actée, [architecture](../../docs/architecture.md#surfaces)), consommée par
`joutio-web-console`. Package `com.joutio.console`, base `joutio_console`, port local 8094.

Lire : `../../AGENTS.md`, la section console du SDK (`libraries/joutio-sdk/AGENTS.md`).

## Architecture

- **Pas de runtime Axon** : dépend de `joutio-microservice-autoconfigure` (web, OAuth2, M2M,
  observabilité) **sans** le starter Axon. `ExceptionAutoConfiguration` du SDK est exclue (ses
  intercepteurs visent les bus) : la gestion d'erreurs est celle de Spring. Pas de label
  `component: axon` dans GitOps.
- **État courant seulement** :
  - tables `console_instance`, `console_processor`, `console_segment`, `console_dlq_stat`,
    `console_audit` ;
  - l'ingestion remplace les lignes filles de l'instance ;
  - `ConsoleCleanupService` supprime les instances muettes depuis 10 min ;
  - les séries temporelles viennent de Prometheus (`joutio.console.prometheus.url`, vide en local =
    désactivé).
- **Actions** : relais M2M (`RestTemplate` OAuth2 `server-client`) vers `/internal/console/**` du
  service ciblé (`joutio.console.services.urls`), puis ligne d'audit (acteur JWT, action, cible,
  résultat).
- **Replay** : le reset exige un processor arrêté. La séquence `pause → reset → start` sur toutes
  les instances est pilotée par la console web.

## Surface REST

| Endpoint | Accès | Rôle |
|---|---|---|
| `POST /internal/ingest/snapshot` | JWT M2M, jamais exposé par l'ingress | instantané d'un agent (`v1`), `204` |
| `GET /api/console/version`, `/dashboard` | `ROLE_ADMIN` | version ; tableau de bord (réplicas, processors, DLQ, débits, volumétrie) |
| `GET /api/console/applications[/{application}]` | `ROLE_ADMIN` | instances ; détail (handlers via `/command-capabilities` et `/query-capabilities`, processors) |
| `GET /api/console/processors?application=` · `/processors/{application}/{instanceId}/{name}` | `ROLE_ADMIN` | processors, segments |
| `GET /api/console/metrics/{applications\|processors\|messages}/…?range=1h\|6h\|24h` | `ROLE_ADMIN` | séries Prometheus (requêtes fixes) |
| `GET /api/console/search/events?application=&aggregateId=` · `/events/by-type` · `/snapshots` | `ROLE_ADMIN` | lecture de l'event store |
| `GET /api/console/scheduled-events?application=` | `ROLE_ADMIN` | deadlines persistantes |
| `GET /api/console/dlq`, `/users`, `/audit?page=&size=` | `ROLE_ADMIN` | stats DLQ, admins et acteurs, journal d'audit |
| `POST …/actions/dlq/{application}/{instanceId}/{processor}/sequences/{sequenceId}/retry` · `DELETE …/sequences/{sequenceId}` · `POST …/clear` | `ROLE_ADMIN` + audit | DLQ |
| `POST …/actions/processors/{application}/{instanceId}/{name}/{pause\|start\|reset}` | `ROLE_ADMIN` + audit | cycle de vie, replay (`{"position":"TAIL\|HEAD\|INSTANT","instant":…}`) |
| `POST …/actions/processors/{application}/{instanceId}/{name}/segments/{segmentId}/{split\|merge}` | `ROLE_ADMIN` + audit | segments |

## Déploiement

- `devops/joutio-gitops/apps/console/`.
- Hôte partagé `console.joutio.com` : l'ingress de la SPA envoie `/api/console` ici ;
  `/internal/**` et `/actuator/**` restent internes.
- Client OIDC public `console` déclaré dans identity.
- Secret `joutio-console-secret` (`seal-secrets.sh console`).

## Commandes

```bash
mvn -pl joutio-console-infrastructure test
```
