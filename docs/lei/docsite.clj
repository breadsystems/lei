(ns lei.docsite
  (:require
   [garden.core :as garden]
   [lei.docsite.style :as style]
   [lei.docs :as docs]
   [lei.core :as core]
   [rum.core :as rum]))

(defn index-html []
  (str
   "<!doctype html>\n"
   (rum/render-static-markup
    (docs/page {:title "Lei 🌺"
                :description "A design system library for Clojure and ClojureScript"
                :styles style/screen
                :head-html
                [:<>
                 [:link {:rel :preconnect
                         :href "https://unpkg.com"}]
                 [:link {:rel :preconnect
                         :href "https://fonts.gstatic.com"}]
                 [:link {:rel :stylesheet
                         :href "https://fonts.googleapis.com/css2?family=Heebo&family=Playfair+Display:ital,wght@1,700&display=swap"}]
                 (docs/inline-style
                  (garden/css style/screen))
                 (docs/inline-style
                  (slurp "docs/highlight.js/styles/tomorrow-night-eighties.css"))
                 (docs/inline-script
                  (slurp "docs/highlight.js/highlight.pack.js"))
                 (docs/inline-script "hljs.initHighlightingOnLoad();")
                 (docs/inline-script
                  (slurp "docs/js/lei.js"))]
                :heading "Lei"
                :subheading "A design system library"
                :sections
                [{:name "Intro"
                  :html-content (docs/path->html "md/intro.md")}
                 (docs/var->map #'core/stack)
                 (docs/var->map #'core/sidebar)
                 (docs/var->map #'core/center)
                 (docs/var->map #'core/cluster)
                 (docs/var->map #'core/switcher)
                 (docs/var->map #'core/cover)
                 (docs/var->map #'core/grid)
                 (docs/var->map #'core/frame)
                 (docs/var->map #'core/reel)
                 (docs/var->map #'core/impostor)
                 (docs/var->map #'core/icon)
                 {:name "Generating Docs"
                  :html-content (docs/path->html "md/docs.md")}]}))))