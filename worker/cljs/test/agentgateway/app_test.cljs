(ns agentgateway.app-test
  "cljs.test coverage for the reagent + re-frame port of the old
  `+page.svelte` metadata card (see docstring of `agentgateway.app`).

  Each test resets `re-frame.db/app-db` in a `:each` fixture so re-frame's
  subscription cache does not leak state between deftests."
  (:require [cljs.test :refer [deftest is use-fixtures]]
            [re-frame.core :as rf]
            [re-frame.db :as rf-db]
            [jp-go-dds.core :as dds]
            [agentgateway.app :as app]))

(use-fixtures :each
  {:before (fn [] (reset! rf-db/app-db {}))
   :after (fn [] (reset! rf-db/app-db {}))})

(deftest initial-db-matches-svelte-source
  ;; Same values `const app = {...}` held in +page.svelte, ported to
  ;; kebab-case cljs keys. `route-count` `0`, `routes` `[]`, `vars` `[]`,
  ;; `xrpc` `true` — nothing renamed in value, only in key shape.
  (is (= {:title "Worker"
          :project "etzhayyim-project-agentgateway"
          :name "worker"
          :kind "worker"
          :route-count 0
          :routes []
          :vars []
          :xrpc true
          :relative-path "60-apps/etzhayyim-project-agentgateway/worker/svelte/src/routes/+page.svelte"}
         app/initial-app-data)))

(deftest initialize-event-populates-subs
  (rf/dispatch-sync [::app/initialize])
  (is (= "Worker" @(rf/subscribe [::app/title])))
  (is (= "worker" @(rf/subscribe [::app/name])))
  (is (= "worker" @(rf/subscribe [::app/kind])))
  (is (= "etzhayyim-project-agentgateway" @(rf/subscribe [::app/project])))
  (is (= 0 @(rf/subscribe [::app/route-count])))
  (is (= true @(rf/subscribe [::app/xrpc?])))
  (is (= [] @(rf/subscribe [::app/routes])))
  (is (= [] @(rf/subscribe [::app/vars])))
  (is (= "60-apps/etzhayyim-project-agentgateway/worker/svelte/src/routes/+page.svelte"
         @(rf/subscribe [::app/relative-path]))))

(deftest view-root-and-container-class
  (rf/dispatch-sync [::app/initialize])
  (let [hiccup (app/main-panel)]
    (is (= :main (first hiccup)))
    (let [container-call (second hiccup)
          container-fn (first container-call)
          container-args (rest container-call)
          rendered (apply container-fn container-args)]
      (is (fn? container-fn))
      ;; jp-go-dds.core/container tags its wrapper div with the dds-ext-*
      ;; layout class (not raw hex / hand-rolled inline style, per the
      ;; workspace UI/UX rule).
      (is (= "dds-ext-container" (get-in rendered [1 :class]))))))
