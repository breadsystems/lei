(ns lei.docs.core
  (:refer-clojure :exclude [rem])
  (:require
   [clojure.java.io :as io]
   [clojure.string :as str]
   [garden.core :as garden]
   [garden.units :as u :refer [ch percent rem]]
   [lei.core :as core]
   [markdown.core :as md]
   [org.httpkit.server :as http]
   [ring.middleware.reload :refer [wrap-reload]]
   [rum.core :as rum]))


(defn md-path->html [path]
  (-> (io/resource path)
      slurp
      md/md-to-html-string))


(def sections
  [{:title "Intro"
    :html-content (md-path->html "md/intro.md")}
   {:title "Stack"
    :content [:<>
              [:p "Soluta elit nulla enim iusto omnis sint. Quas deserunt cillum lorem, voluptas excepturi lorem et excepteur. Placeat in nostrud voluptas magna. Similique est consequat ducimus consectetur minim, harum exercitation molestias duis ad. Pariatur irure minus rerum, vero ex duis voluptas eos eligendi ducimus. Expedita ullamco animi excepturi distinctio cumque, aute possimus voluptate anim corrupti rerum at."]]}
   {:title "Sidebar"
    :content [:<> [:p "Libero fugiat similique accusamus harum cupidatat molestias consequat do qui mollitia."]]}])

(defn- slug [s]
  (str/lower-case (str/replace s #" " "-")))

(defn- inline-style [& styles]
  [:style {:dangerouslySetInnerHTML {:__html (garden/css styles)}}])

(defn docsite [_]
  {:status 200
   :headers {"content-type" "text/html"}
   :body (rum/render-static-markup [:html
   [:head
    [:meta {:charset "utf-8"}]
    [:title "Lei 🌺"]
    [:meta {:name "viewport" :content "width=device-width, initial-scale=1"}]
    (inline-style (core/axioms)
                  [:* {:font-family :sans-serif}]
                  [:header {:text-align :center}]
                  [:h1
                   [:&:after {:content "'🌺'"
                              :display :inline-block
                              :margin-left (rem 1)}]]
                  [:h2 :h3 :h4 :h5 :h6 {:margin-top 0}]
                  (core/stack)
                  (core/sidebar {:space (rem 1)
                                 :content-min-width (percent 60)
                                 :sidebar-width (ch 20)}))]
   [:body
    [:header
     [:h1 "Lei"]
     [:h2 "A design system library"]]
    [:div.with-sidebar
     [:div
      [:nav
       [:ul
        (for [{:keys [title]} sections
              :let [link (str "#" (slug title))]]
          [:li [:a {:href link} title]])]]
      [:div.stack
       (for [{:keys [title content html-content]} sections
             :let [link (slug title)]]
         [:section
          [:a {:name link}]
          [:div [:a {:href "#"} "Top"]]
          [:h3 title]
          (if html-content
            [:div {:dangerouslySetInnerHTML {:__html html-content}}]
            [:div content])])]]]]])})

(defonce server (atom nil))

(defn- start! []
  (println "🌺 Running at http://localhost:8001")
  (reset! server
          (http/run-server
           (wrap-reload #'docsite {:dirs ["src" "dev"]})
           {:port 8001})))

(defn- stop! []
  (when-not (nil? server)
    (@server :timeout 100)
    (reset! server nil)))

(comment
  (start!)
  (stop!)
  (do (stop!) (start!)))

(defn -main []
  (start!))