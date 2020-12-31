(ns lei.docsite.dev
  (:require
   [clojure.java.io :as io]
   [lei.docsite :as d]
   [lei.gen :as gen]
   [org.httpkit.server :as http]
   [ring.middleware.reload :refer [wrap-reload]]))

(defonce ^:private server (atom nil))
(defonce ^:private watchers (atom nil))

;; Really, really dumb static file server
(defn- app [{:keys [uri]}]
  (let [paths {"/" "/index.html"}]
    {:status 200
     :headers {"content-type" "text/html"}
     :body (as-> (get paths uri uri) $
             (str "dev" $)
             (io/file $)
             (slurp $))}))

(defn- start! []
  (when-not (.exists (io/file "dev/index.html"))
    (spit "dev/index.html"
          "No output yet! Make a change to see your generated docs."))
  (reset! watchers (gen/watch!
                        #{"src" "docs"}
                        (fn []
                          (require 'lei.docs :reload)
                          (require 'lei.docsite.style :reload)
                          (require 'lei.docsite :reload)
                          (require 'lei.core :reload)
                          (d/index-html))
                        {:path "dev/index.html"}))
  (println "🌺 Serving from /dev at http://localhost:8001")
  (reset! server
          (http/run-server
           (wrap-reload #'app {:dirs ["src" "docs"]})
           {:port 8001}))
  nil)

(defn- stop! []
  (when-not (nil? @server)
    (@server :timeout 100)
    (reset! server nil))
  (when (seq @watchers)
    (gen/stop-watching! @watchers)
    (reset! watchers nil)))

(defn- running? []
  (boolean @server))

(comment
  (d/index-html)

  (running?)
  (start!)
  (stop!)
  (do (stop!) (start!)))

(defn -main []
  (start!))