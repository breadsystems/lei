(ns lei.lei-docsite.dev
  (:require
   [lei.lei-docsite.core :as docsite]
   [org.httpkit.server :as http]
   [ring.middleware.reload :refer [wrap-reload]]))

(defonce server (atom nil))

(defn- start! []
  (println "🌺 Running at http://localhost:8001")
  (reset! server
          (http/run-server
           (wrap-reload #'docsite/docsite {:dirs ["src" "dev"]})
           {:port 8001})))

(defn- stop! []
  (when-not (nil? server)
    (@server :timeout 100)
    (reset! server nil)))

(comment
  (start!)
  (stop!)
  (do (stop!) (start!)))

(defn -main []
  (start!))