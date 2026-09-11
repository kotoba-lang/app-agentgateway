(ns agentgateway.app
  "agentgateway worker frontend — single-page app (ADR-2608080100).

  Ported from `worker/svelte/src/routes/+page.svelte`, a static SvelteKit page
  that rendered one 'app metadata card' — the worker's title/project/name/kind,
  its declared public route count, its runtime bindings, whether the XRPC
  proxy is enabled, and the source path of the page that described it. There
  was no interactivity, no client-side routing, and no second document; the
  only backend piece SvelteKit shipped alongside it was the XRPC → MCP-router
  proxy at `src/routes/xrpc/[...path]/+server.ts`, which this migration
  preserves byte-identical (below a header) at `../src/xrpc-proxy.ts` — not
  wired into the worker, since that is a product decision, not a migration
  step.

  This namespace reproduces that same single card as reagent + re-frame,
  rendered with `jp-go-dds` (デジタル庁デザインシステム / DADS) components
  instead of the hand-rolled `<style>` block the Svelte file carried. The app
  is exactly one view mounted at exactly one `#app` node in exactly one
  document — `public/index.html`, generated ahead of time by
  `scripts/gen-page.cljs` from `jp-go-dds.page/->page` (there is nothing here
  for a router to be needed for; a router only enters this codebase's design
  rule at a *third* app, see the workspace UI/UX rule).

  Data flow: `::initialize` seeds `db/app-data` into app-db once at boot;
  every other key here is a `re-frame.core/reg-sub` reading a scalar or
  collection out of that one map, and `main-panel` renders each sub through
  a `jp-go-dds.core` primitive. There is no other state — this page has
  nothing to mutate."
  (:require [reagent.core :as r]
            [reagent.dom.client :as rdom]
            [re-frame.core :as rf]
            [jp-go-dds.core :as dds]))

;; --- data ---------------------------------------------------------------
;;
;; Same values the Svelte file's inline `const app = {...}` held. `xrpc` is a
;; fact about the worker's route table (the XRPC proxy endpoint is declared),
;; not a claim about `xrpc-proxy.ts` being wired into the deployed worker.

(def initial-app-data
  {:title "Worker"
   :project "etzhayyim-project-agentgateway"
   :name "worker"
   :kind "worker"
   :route-count 0
   :routes []
   :vars []
   :xrpc true
   :relative-path "60-apps/etzhayyim-project-agentgateway/worker/svelte/src/routes/+page.svelte"})

;; --- events ---------------------------------------------------------------

(rf/reg-event-db
 ::initialize
 (fn [_ _]
   {:app-data initial-app-data}))

;; --- subs -------------------------------------------------------------------

(rf/reg-sub
 ::app-data
 (fn [db _] (:app-data db)))

(rf/reg-sub
 ::title
 :<- [::app-data]
 (fn [app-data _] (:title app-data)))

(rf/reg-sub
 ::name
 :<- [::app-data]
 (fn [app-data _] (:name app-data)))

(rf/reg-sub
 ::kind
 :<- [::app-data]
 (fn [app-data _] (:kind app-data)))

(rf/reg-sub
 ::project
 :<- [::app-data]
 (fn [app-data _] (:project app-data)))

(rf/reg-sub
 ::route-count
 :<- [::app-data]
 (fn [app-data _] (:route-count app-data)))

(rf/reg-sub
 ::xrpc?
 :<- [::app-data]
 (fn [app-data _] (:xrpc app-data)))

(rf/reg-sub
 ::routes
 :<- [::app-data]
 (fn [app-data _] (:routes app-data)))

(rf/reg-sub
 ::vars
 :<- [::app-data]
 (fn [app-data _] (:vars app-data)))

(rf/reg-sub
 ::relative-path
 :<- [::app-data]
 (fn [app-data _] (:relative-path app-data)))

;; --- views --------------------------------------------------------------

(defn- top-section []
  [dds/section {}
   [:p (str "Cloudflare " @(rf/subscribe [::kind]))]
   [dds/heading 1 @(rf/subscribe [::title])]
   [:span @(rf/subscribe [::name])]])

(defn- facts-row []
  (let [project @(rf/subscribe [::project])
        route-count @(rf/subscribe [::route-count])
        xrpc? @(rf/subscribe [::xrpc?])]
    [dds/grid {:min "180px"}
     [dds/card [:span "Project"] [:strong project]]
     [dds/card [:span "Routes"] [:strong (str route-count)]]
     [dds/card [:span "XRPC"] [:strong (if xrpc? "enabled" "not configured")]]]))

(defn- routes-panel []
  (let [routes @(rf/subscribe [::routes])]
    [dds/section {:title "Public Routes"}
     (if (seq routes)
       [:ul (for [route routes] ^{:key route} [:li route])]
       [:p "No public route is declared next to this app surface."])]))

(defn- vars-panel []
  (let [vars @(rf/subscribe [::vars])]
    [dds/section {:title "Runtime Bindings"}
     (if (seq vars)
       [dds/row (for [v vars] ^{:key v} [dds/chip-label v])]
       [:p "No public vars are declared in the nearest wrangler config."])]))

(defn- source-panel []
  [dds/section {:title "Source"}
   [:p @(rf/subscribe [::relative-path])]])

(defn main-panel []
  [:main
   [dds/container
    [top-section]
    [facts-row]
    [routes-panel]
    [vars-panel]
    [source-panel]]])

;; --- mount ----------------------------------------------------------------

(defonce root (atom nil))

(defn- mount-root! []
  (when-not @root
    (reset! root (rdom/create-root (.getElementById js/document "app"))))
  (rdom/render @root [main-panel]))

(defn ^:export main []
  (rf/dispatch-sync [::initialize])
  (mount-root!))
