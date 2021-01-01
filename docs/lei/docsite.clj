(ns lei.docsite
  (:require
   [clojure.java.io :as io]
   [garden.core :as garden]
   [lei.docsite.style :as style]
   [lei.docs :as docs]
   [lei.core :as core]
   [rum.core :as rum]))

(defn- styles []
  (str
   (garden/css style/screen)
   (slurp (io/resource "js/highlight.js/styles/tomorrow-night-eighties.css"))))

(defn- scripts []
  (str (slurp (io/resource "js/highlight.js/highlight.pack.js"))
       (slurp (io/resource "js/lei.js"))))

(defn index-html []
  (str
   "<!doctype html>\n"
   (rum/render-static-markup
    (docs/page {:title "Lei 🌺"
                :description "A design system library for Clojure and ClojureScript"
                :head-html
                [:<>
                 [:link {:rel :preconnect
                         :href "https://unpkg.com"}]
                 [:link {:rel :preconnect
                         :href "https://fonts.gstatic.com"}]
                 [:link {:rel :stylesheet
                         :href "https://fonts.googleapis.com/css2?family=Heebo&family=Playfair+Display:ital,wght@1,700&display=swap"}]
                 (docs/inline-style (styles))
                 (docs/inline-script (scripts))]
                :header-html
                [:<>
                 [:h1 "Lei"]
                 [:h2 "A design system library"]]
                :sections
                [{:name "Intro"
                  :html-content (docs/path->html "md/intro.md")}
                 {:name "Getting Started"
                  :html-content (docs/path->html "md/getting-started.md")}
                 {:name "Generating Docs"
                  :html-content (docs/path->html "md/docs.md")}
                 ;; TODO full docs API reference
                 ;; TODO Actual IA/ToC
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
                 (docs/var->map #'core/icon)]}))))