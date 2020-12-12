(ns lei.docsite.dev
  (:require
   [lei.docsite :as d]
   [org.httpkit.server :as http]
   [ring.middleware.reload :refer [wrap-reload]]))

(defonce server (atom nil))

(defn- app [_]
  {:status 200
   :headers {"content-type" "text/html"}
   :body (d/index-html)})

(defn- start! []
  (println "🌺 Running at http://localhost:8001")
  (reset! server
          (http/run-server
           (wrap-reload #'app {:dirs ["src" "dev"]})
           {:port 8001})))

(defn- stop! []
  (when-not (nil? server)
    (@server :timeout 100)
    (reset! server nil)))

(comment
  (d/index-html)

  (start!)
  (stop!)
  (do (stop!) (start!)))

(defn -main []
  (start!))