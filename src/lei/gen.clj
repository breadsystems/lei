(ns lei.gen
  (:require
   [clojure.java.io :as io]
   [clojure.string :as str]
   [juxt.dirwatch :as watch]))

(defn generate!
  ([handler]
   (generate! handler {}))
  ([handler opts]
   (let [{:keys [path]} (merge {:path "dist/index.html"} opts)
         html (handler)]
     (spit path html)
     (println (format "Wrote %d bytes to %s" (count html) path))
     nil)))

(defn watch! [dir handler opts]
  (println (format "Watching %s for changes..." dir))
  (watch/watch-dir (fn [_]
                     (generate! handler opts))
                   (io/file dir)))

(defn stop-watching! [w]
  (println "Stopping watch.")
  (watch/close-watcher w))

(defn -main [handler-name & args]
  {:pre [(even? (count args))]}
  (let [keywordize (fn [[k v]]
                     [(keyword (str/replace k #"^:" "")) v])
        opts (into {} (map keywordize (partition 2 args)))
        handler-sym (symbol handler-name)]

    (println "Loading" (namespace handler-sym))
    (require (symbol (namespace handler-sym)))

    (let [handler (deref (resolve handler-sym))]
      (generate! handler opts)
      nil)))

(comment
  (-main "lei.docsite/index-html" "path" "dist/main.html"))