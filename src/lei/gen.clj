(ns lei.gen
  (:require
   [clojure.string :as str]))

(defn generate
  ([handler]
   (generate handler {}))
  ([handler opts]
   (let [sym (symbol handler)
         {:keys [path]}
         (merge {:path "dist/index.html"} opts)]

    ;; In a long-running task (i.e. the dev server),
    ;; we may have previously loaded the handler's ns.
     (println "Loading" (namespace sym))
     (require (symbol (namespace sym)) :reload)

     (let [;; Dereference and immediately call our handler.
           html ((deref (resolve sym)))]
       (spit path html)
       (println (format "Output %d bytes to %s" (count html) path))))))

(defn -main [handler & args]
  {:pre [(even? (count args))]}
  (let [keywordize (fn [[k v]]
                     [(keyword (str/replace k #"^:" "")) v])
        opts (into {} (map keywordize (partition 2 args)))]
    (generate handler opts)))