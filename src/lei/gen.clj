(ns lei.gen
  (:require
   [clojure.core.async :refer [alts! chan close! go go-loop timeout <! >!]]
   [clojure.java.io :as io]
   [clojure.string :as str]
   [juxt.dirwatch :as watch]))

(defn- debounce [in ms]
  (let [out (chan)]
    (go-loop [last-val nil]
      (let [val   (if (nil? last-val) (<! in) last-val)
            timer (timeout ms)
            [new-val ch] (alts! [in timer])]
        (condp = ch
          timer (do (when-not
                     (>! out val)
                      (close! in))
                    (recur nil))
          in (when new-val (recur new-val)))))
    out))

(defn generate!
  ([handler]
   (generate! handler {}))
  ([handler opts]
   (let [{:keys [path]} (merge {:path "dist/index.html"} opts)]
     (try
       (let [html (handler)]
         (spit path html)
         (println (format "Wrote %d bytes to %s" (count html) path)))
       (catch java.lang.Throwable ex
         (println (format "Handler threw an exception: %s"
                          (.getMessage ex)))))
     nil)))

(defn watch! [dirs handler opts]
  (println (format "Watching %s for changes..." (str/join ", " dirs)))
  (let [ch (chan)
        debounced (debounce ch (or (:debounce-ms opts) 100))]
    (go-loop []
      (let [args (<! debounced)]
        (apply generate! args)
        (recur)))
    (for [dir dirs]
      (watch/watch-dir (fn [_]
                         (go (>! ch [handler opts])))
                       (io/file dir)))))

(defn stop-watching! [watchers]
  (println "Stopping watch.")
  (for [w watchers]
    (watch/close-watcher w)))

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