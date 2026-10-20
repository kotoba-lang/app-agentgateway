# app-agentgateway（JSON-RPC 判定）— itonami（dapp）上の分散型設計

状態: 第 1 スライス実装（2026-10-20）。正本はこの文書。
追跡: Linear JUN-128（親 JUN-116）。

## 位置づけ

app-agentgateway は **MCP router エントリポイント**（`/xrpc/com.etzhayyim.mcp.message`
—— 各 app の AGENTGATEWAY_MCP_ROUTER_URL が指す先）。JSON-RPC 2.0 で
tool 一覧・tool 呼び出しを受ける。

## このスライス: JSON-RPC 判定コア

| 層 | 実体 | 状態 |
|---|---|---|
| 判定コア guest | `aiueos/gateway_rpc.cljk` — request-judgment（**jsonrpc '2.0' と method が必須 = -32600**）/ method-judgment（既知 method のみ、未処理は **-32601**）/ error-code（**JSON-RPC 2.0 標準 4 コード**: -32700 / -32600 / -32601 / -32602）/ parse-judgment（**-32700 Parse error**） | **実装済み・実測** |
| 記述子 | `aiueos/gateway-rpc.edn` | **実装済み** |
| parity | `worker/cljs/test/gateway/rpc_parity_test.cljk` — 正本（worker/src/index.ts）の検証・エラーコード・method 分岐と一致 | **実装済み・実測** |

実測: `amu test aiueos/gateway_rpc.cljk` → **15/15**（jvm-kir / js / wasm）。
parity → 7 assertions / 0 failures。

## 正本の正確な規則（guest はこれに追従）

- **JSON-RPC 2.0 検証**: `body.jsonrpc !== '2.0' || !body.method` なら
  **`{code: -32600, message: 'Invalid Request'}`（400）**。
- **JSON パース失敗**は `{code: -32700, message: 'Parse error'}`。
- method 分岐: `tools/list`（tool 一覧）/ `tools/call`（tool 名検証、
  現状 `agentgateway.ping` のみ）/ 未処理 method は default error（-32601 系）。
- エラーコードは **JSON-RPC 2.0 標準の閉じた 4 種**（-32700 / -32600 /
  -32601 / -32602）—— 未知 kind にコードを発明しない（guest の fail-closed）。

## 未実装境界

- 実際の routing / discovery（現在は stub —「TODO: Implement actual routing
  and discovery logic」）。
- tool 実行の委譲（各 app への配送）と認可。
- mon（DNA）化、aiueos session 載せ（fleet 共通）。
