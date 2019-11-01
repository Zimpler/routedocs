[![Clojars Project](https://img.shields.io/clojars/v/com.zimpler/routedocs.svg)](https://clojars.org/com.zimpler/routedocs)

# routedocs

Lightweight docs for Compojure routes


## Introduction

A set of drop-in replacement macros for documenting routes/endpoints.

Markdown/HTML may optionally be served at a preferred endpoint (a la Swagger).

Markdown is optionally written to specified file, which can then be committed as part of source documentation.

All endpoints using the routedocs macros are automatically listed.

You may provide a docstring for each endpoint (or ``nil`).  The docstring may contain Markdown.  
If `nil`, then the route will still be listed, but without docstring.


## Usage

See [src_test/testing/test_app.clj] for usage.

See [test_docs/routes.md] for resulting doc.

Note: You may mix and match Compojure routes and Routedocs routes.  
The Compojure routes will not be include in the listing.

Note: If you have Routedocs routes in a "context", then you must use `context+` (with or without docstring).


## License

Copyright © 2019 Zimpler

Distributed under the Eclipse Public License, the same as Clojure.