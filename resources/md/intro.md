Lei is a library for Clojure and ClojureScript for building visual design systems. It provides simple abstractions for building CSS layouts with [Garden](https://github.com/noprompt/garden) alongside [Hiccup](https://github.com/weavejester/hiccup)-style markup data (HTML tags and their children as vectors; sets of properties as maps). Heavily inspired by *[Every Layout](https://every-layout.dev/)*, Lei was built to help you design reusable *aspects* of layout components that *compose* with each other into a cohesive whole.

Design systems built in Lei offer several advantages over more traditional component libraries, such as Bootstrap:

1. **Less code overall.** "Importing" a pattern or component from Lei is just an ordinary Clojure `require`. If you don't need a certain pattern, just don't import it and its CSS won't get served.
2. **Tighter CSS.** The principles of *Every Layout* allow for CSS and markup that is simpler by an order of magnitude than big, class-centric frameworks like Bootstrap and Foundation.
3. **It's just data.** Because Lei represents your components and the CSS describing them as regular Clojure vectors and maps, you can use Lei as a foundation and manipulate the styles and markup it generates however you like.
4. **No separate toolchain.** You don't need to concatenate all of Framework X into your CSS bundle, manage SASS variables, or mess with Webpack. Just render HTML and CSS with good ol' Clojure libraries.
5. **Lei is self-documenting.** Lei comes with generic facilities for documenting your own patterns. Just pass in some vectors and maps that Garden and Hiccup (or your Hiccup-like library of choice) will understand, and get nicely formatted Clojure, HTML, and CSS on your pattern library docs pages.