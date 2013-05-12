(in-ns 'rserve-cli2.rconnection)



(defn sexp-make-empty-data-frame
  "Makes an empty R data frame."
  []
  (ref (RserveCLI2.Sexp/MakeDataFrame nil nil)))



;;         public static Sexp Make( object x )
;;         {
;;             if ( x is Sexp                         ) { return       ( Sexp )x;                           }
;;             if ( x is bool?                        ) { return Make( ( bool? )x                        ); }
;;             if ( x is IEnumerable<bool?>           ) { return Make( ( IEnumerable<bool?> )x           ); }
;;             if ( x is double                       ) { return Make( ( double )x                       ); }
;;             if ( x is IEnumerable<double>          ) { return Make( ( IEnumerable<double> )x          ); }
;;             if ( x is double[ , ]                  ) { return Make( ( double[ , ] )x                  ); }
;;             if ( x is decimal                      ) { return Make( ( decimal )x                      ); }
;;             if ( x is IEnumerable<decimal>         ) { return Make( ( IEnumerable<decimal> )x         ); }
;;             if ( x is decimal[ , ]                 ) { return Make( ( decimal[ , ] )x                 ); }
;;             if ( x is int                          ) { return Make( ( int )x                          ); }
;;             if ( x is IEnumerable<int>             ) { return Make( ( IEnumerable<int> )x             ); }
;;             if ( x is int[ , ]                     ) { return Make( ( int[ , ] )x                     ); }
;;             if ( x is DateTime                     ) { return Make( ( DateTime )x                     ); }
;;             if ( x is IEnumerable<DateTime>        ) { return Make( ( IEnumerable<DateTime> )x        ); }
;;             if ( x is string                       ) { return Make( ( string )x                       ); }
;;             if ( x is IEnumerable<string>          ) { return Make( ( IEnumerable<string> )x          ); }
;;             if ( x is IDictionary<string , object> ) { return Make( ( IDictionary<string , object> )x ); }
;;             throw new ArgumentException( string.Format( "no auto conversion rule for type {0} to Sexp." ,
;;                                                                                          x.GetType().Name ) );



(defn sexp-array-with-fn
  "Returns an R expression using the given constructor, applies optionally a type conversion."
  ([v f]
     (let [r (ref (f))]
       (dosync (doseq [s v] (.Add @r s))) r))
  ([v f convert]
     (let [r (ref (f))]
       (dosync (doseq [s v] (.Add @r (convert s))) r))))


(defn sexp-of-string-array
  "Returns an R expression on the given vector of strings."
  [v]
  (sexp-array-with-fn v (fn [] (RserveCLI2.SexpArrayString.))))


(defn sexp-of-double-array
  "Returns an R expression on the given double vector."
  [v]
  (sexp-array-with-fn v (fn [] (RserveCLI2.SexpArrayDouble.))))


(defn sexp-of-decimal-array
  "Returns an R expression of type Double on the given decimal vector."
  [v]
  (sexp-array-with-fn v (fn [] (RserveCLI2.SexpArrayDouble.)) double))


(defn sexp-of-int-as-double-array
  "Returns an R expression of type Double on the given integer vector."
  [v]
  (sexp-array-with-fn v (fn [] (RserveCLI2.SexpArrayDouble.)) double))


(defn sexp-of-int-array
  "Returns an R expression on the given integer vector."
  [v]
  (sexp-array-with-fn v (fn [] (RserveCLI2.SexpArrayInt.)) int))


(defn sexp-get-hash-code
  "Returns the hash code of the given R expression."
  [sexp]
  (.GetHashCode sexp))


(defn sexp-get-length
  "Returns the length of the n-th element in the R expression."
  [sexp n]
  (.GetLength sexp (int n)))


(defn sexp-add
  "Adds a key, value pair to the R expression; key must be a string."
  [sexp k v]
  (if (string? k)
    (let [kvp
          (|System.Collections.Generic.KeyValuePair`2[System.String, System.Object]|. k v)]
      (.Add sexp kvp))
    (throw (ArgumentException. "invalid type (must be string) of key ", k))))


(defn sexp-contains?
  "Indicates whether the R expression contains the key, value pair; key must be a string."
  [sexp k v]
  (if (string? k)
    (let [kvp
          (|System.Collections.Generic.KeyValuePair`2[System.String, System.Object]|. k v)]
      (.Contains sexp kvp))
    (throw (ArgumentException. "invalid type (must be string) of key ", k))))


(defn sexp-remove
  "removes a key, value pair from the R expression; key must be a string."
  [sexp k v]
  (if (string? k)
    (let [kvp
          (|System.Collections.Generic.KeyValuePair`2[System.String, System.Object]|. k v)]
      (.Remove sexp kvp))
    (throw (ArgumentException. "invalid type (must be string) of key ", k))))
