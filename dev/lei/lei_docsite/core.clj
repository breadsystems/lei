(ns lei.lei-docsite.core
  (:require
   [lei.lei-docsite.style :as style]
   [lei.docs :as docs]
   [lei.core :as core]
   [rum.core :as rum]))

(defn docsite [_]
  {:status 200
   :headers {"content-type" "text/html"}
   :body
   (rum/render-static-markup
    (docs/render {:title "Lei 🌺"
                  :styles style/screen
                  :heading "Lei"
                  :subheading "A design system library"
                  :sections
                  [{:name "Intro"
                    :html-content (docs/path->html "md/intro.md")}
                   (docs/var->docs #'core/stack)
                   (docs/var->docs #'core/sidebar)]}))})