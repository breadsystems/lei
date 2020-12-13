(ns lei.util
  (:refer-clojure :exclude [*])
  (:require
   [clojure.string :as str]
   [garden.selectors :as s]))

(s/defselector *)

(defn root [m]
  {:pre [(map? m)]}
  [":root" m])

(defmacro defutil [sym & [attrs]]
  (let [s (str sym)
        attrs (or attrs (let [[k v] (str/split s #":")] {k v}))
        ;; Replace : with \\:
        ;; ...replace = with \\=
        ;; ...and prepend a dot
        ->class (comp #(str/replace % #"(=|:)" "\\\\$1")
                      #(if (str/starts-with? % ".") % (str "." %)))
        class (->class s)]
    `(do (def ~sym [~class ~attrs]))))

(comment
  (macroexpand '(defutil text:small {:font-size "0.8em"}))
  (macroexpand '(defutil text-align:center)))

(defn code-fonts
  "Default fonts for the code and pre tags."
  []
  [:code :pre {:font-family ["Courier New" "Courier" "monospace"]}])

(def system-serif
  ["Georgia" "Times" "Times New Roman" "serif"])

(def system-sans
  ["-apple-system"
   "BlinkMacSystemFont"
   "Segoe UI"
   "Roboto"
   "Oxygen-Sans"
   "Ubuntu"
   "Cantarell"
   "Helvetica Neue"
   "sans-serif"])