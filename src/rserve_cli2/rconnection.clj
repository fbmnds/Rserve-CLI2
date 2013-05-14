;; Copyright (c) Friedrich Boeckh, 2013.
;; Distributed under the Eclipse Public License, the same as Clojure.

;; Rincanter CLI-to-R bridge.

;; Inspired by Joel Boehland´s Rincanter JVM-to-R bridge.
;; http://github.com/jolby/rincanter


(ns rserve-cli2.rconnection)


;;   (:import (org.rosuda.REngine REXP RList REXPGenericVector
;;                                REXPInteger REXPDouble REXPString REXPLogical
;;                                RFactor REXPFactor
;;                                REngineException REXPMismatchException)
;;            (org.rosuda.REngine.JRI JRIEngine)
;;            (org.rosuda.JRI RMainLoopCallbacks))
;;   (:use [incanter.core]
;;         [com.evocomputing.rincanter.convert]))


(defonce ^:dynamic *rserve-connection*
  (do
  (comment
  "Generates a ref for the RserveCLI2 server, if RserveCLI2 successfully loaded, or dies.
   Policy for loading RserveCLI2:
   1. load './RserveCLI2.dll'
   2. load '$RSERVE_CLI2'
   3. load 'c:/clojure-clr/lib/RserveCLI2.dll'"
  )
  (if (System.Reflection.Assembly/LoadFrom
       (or "RserveCLI2.dll"
           (get (System.Environment/GetEnvironmentVariables) "RSERVE_CLI2")
           "file:///c:/clojure-clr/lib/RserveCLI2.dll"))
    (ref nil)
    (throw (Exception.
            "RserveCLI2.dll not found in paths [\".\", RESERVE_CLI2, c:/clojure-clr/lib]")))))


(defn- init-with-ip
  "Connect to RserveCLI2 using the server IP address;
  fail if a connection is already established."
  [ipaddress port user password]
  ;;check for pre-existing connection
  (when @*rserve-connection*
    (throw (Exception. "reconnects to RserveCLI2 are not permitted")))
  (let [valid-ipaddress (or (System.Net.IPAddress. (byte-array ipaddress))
                            (throw (ArgumentException.
                                    "invalid server IP address ", ipaddress)))]
    (dosync
     (ref-set *rserve-connection*
              {:cursor
               (RserveCLI2.RConnection. valid-ipaddress port user password)}))))


(defn- init-with-hostname
  "Connect to RserveCLI2 using the server IP address;
  fail if a connection is already established."
  [hostname port user password]
  ;;check for pre-existing connection
  (when @*rserve-connection*
    (throw (Exception. "reconnects to RserveCLI2 are not permitted")))
  (dosync
   (ref-set *rserve-connection*
            {:cursor
             (RserveCLI2.RConnection. hostname port user password)})))


(defn- not-empty-or-nil
  "Gets value of key k from map m; returns nil, if v is empty."
  [m k]
  (let [v (get (apply assoc {} m) k)]
    (if (empty? v) nil v)))


(defn init
  "Initializes the RserveCLI2 connection using a configuration map."
  [{:as m
    :keys [server port user password]
    :or {server [127 0 0 1]
         port 6311
         ; deal with empty user/password strings
         user (not-empty-or-nil m :user)
         password (not-empty-or-nil m :password)}}]
  (cond (string? server) (init-with-hostname server port user password)
        (vector? server) (init-with-ip server port user password)
        :else (throw (ArgumentException. "invalid server", server))))


(defn open
  "Alias for 'rserve.cli2.rconnection/init'."
  [m]
  (init m))


(defn- get-rc
  "Gets the RserveCLI2 connection or dies."
  []
  (if @*rserve-connection*
    (@*rserve-connection* :cursor)
    (throw (Exception. "unable to get the connection to RserveCLI2"))))


(defn dispose
  "Disposes the RserveCLI2 connection."
  []
  (dosync
   (ref-set *rserve-connection*
            {:cursor (and (.Dispose (get-rc)) nil)})
   (ref-set *rserve-connection* nil)))

(defn close
  "Alias for 'rserve.cli2.rconnection/dispose'."
  []
  (dispose))


(defn assign
  "Assigns a label to an R expression on the R server."
  [label sexp]
  (.Assign (get-rc) label sexp))


(defn evaluate
  "Evaluates an R command string, returns the result."
  [cmd]
  (.Eval (get-rc) cmd))


(defn evaluate-remote-only
  "Evaluates an R command string, does not return the result."
  [cmd]
  (.VoidEval (get-rc) cmd))


(defn read-file
  "Reads a file from R server"
  [from-file & to-file]
  (let [f (System.IO.File/Create (or to-file from-file))]
    (.CopyTo (.ReadFile (get-rc) from-file) f)
    (.Dispose f)))


(defn remove-file
  "Deletes a file on R server"
  [file-name]
  (.RemoveFile (get-rc) file-name))


(defn write-file
  "Writes a file to the R server."
  [file-name stream]
  (.WriteFile (get-rc) file-name stream))


(defn copy-to
  ".NET 4's CopyTo"
  [from to]
  (.CopyTo (get-rc) from to))
