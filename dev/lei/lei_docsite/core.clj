(ns lei.lei-docsite.core
  (:require
   [clojure.java.io :as io]
   [clojure.string :as str]
   [garden.core :as garden]
   [lei.lei-docsite.style :as style]
   [lei.core :as core]
   [markdown.core :as md]
   [rum.core :as rum]))


(defn md-path->html [path]
  (-> (io/resource path)
      slurp
      md/md-to-html-string))

(defn- slug [& ss]
  (str/lower-case
   (str/join "-" (map #(str/replace % #" " "-") (filter string? ss)))))

(defn- anchor [& ss]
  (str "#" (apply slug ss)))

(defn heading [tag & sections]
  [:<>
   [:a {:name (apply slug sections)}]
   [:a {:href (apply anchor sections)} [tag (last sections)]]])

(defn docs [data]
  (let [{:lei/keys [name description options examples]
         :keys [doc]}
        data
        section-name name]
    [:article
     (heading :h2 section-name)
     [:p (str (or description doc))]
     [:section
      (heading :h3 name "Examples")
      [:<> (for [{:keys [name form desc]} examples]
             [:section
              (heading :h4 section-name "examples" name)
              [:p desc]
              ;; TODO code formatting
              [:pre (str form)]
              [:p "Result:"]
              [:pre (str (eval form))]])]]
     [:section
      (heading :h3 name "Options")
      [:<> (for [{:keys [name desc default]} options]
             [:section
              (heading :h4 section-name "options" name)
              [:p desc " "
               (when default [:strong "Default: " [:code default]])]])]]]))

(defn var->docs [v]
  (let [m (meta v)]
    {:name (:lei/name m)
     :content (docs m)}))

(def sections
  [{:name "Intro"
    :html-content (md-path->html "md/intro.md")}
   (var->docs #'core/stack)
   (var->docs #'core/sidebar)])


(defn docsite [_]
  {:status 200
   :headers {"content-type" "text/html"}
   :body
   (rum/render-static-markup
    [:html
     [:head
      [:meta {:charset "utf-8"}]
      [:title "Lei 🌺"]
      [:meta {:name "viewport" :content "width=device-width, initial-scale=1"}]
      [:style {:dangerouslySetInnerHTML {:__html (garden/css style/screen)}}]]
     [:body
      [:header
       [:h1 "Lei"]
       [:h2 "A design system library"]]
      [:div.with-sidebar
       [:div
        [:nav
         [:ul
          (for [{:keys [name]} sections]
            [:li [:a {:href (slug name)} name]])]]
        [:div.stack
         (for [{:keys [name content html-content]} sections]
           [:div
            (if html-content
              [:<>
               (heading :h2 name)
               [:div {:dangerouslySetInnerHTML {:__html html-content}}]]
              [:<> content])
            [:div [:a {:href "#"} "️↑ Top"]]])]]]]])})

(comment
  (docsite {}))
