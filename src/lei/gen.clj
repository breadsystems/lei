(ns lei.gen)

(defn -main [generate & [path]]
  (let [sym (symbol generate)]
    (println "Requiring" (namespace sym))
    (require (symbol (namespace sym)))
    (let [render (deref (resolve sym))
          html (render)
          out (or path "dist/index.html")]
      (spit out html)
      (println (format "Output %d bytes to %s" (count html) out)))))