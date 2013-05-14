;;
(load-file "./src/rserve_cli2/rconnection.clj")

;;
(load-file "./src/rserve_cli2/rconnection_sexp.clj")

;;
(require ['rserve-cli2.rconnection :as 'rc])


;; clean up an eventually existing connection
(try (rc/close) (catch Exception e nil))


;;
(def m {:server [192 168 56 102] :port 6311 :user nil :password nil})
;;
(rc/open m)


;;
(def d (ref nil))
(dosync (ref-set d (rc/sexp-make-empty-data-frame)))
(dosync (rc/assign "d" @d))



;;
(def x (rc/sexp-of-int-array (vec (range 1 21))))
(def y (rc/sexp-of-double-array (vec (map #(+ 2 (* 0.5 % %)) (range 1 21)))))


(rc/sexp-add @d "x" @x)
(rc/sexp-add @d "y" @y)

;; send to server
;;
(rc/assign "d" @d)
;;
(rc/evaluate "ls()")
(rc/evaluate "d$x")
(rc/evaluate "d$y")

;; from local to server  =/=   already on the server
;;       |                            |
;;       v                            v
;; (rc/assign "z" (rc/evaluate "(lm(y ~ x, d))"))


;; z is created on the server, assigning z is neither possible nor necessary
;;
(rc/evaluate-remote-only "z <- (lm(y ~ x, d))")
(rc/evaluate-remote-only "sum_z <- summary(z)")


;; verify that d, z, sum_z  are on the server
;;
(rc/evaluate "ls()")

;; show remote results
;;
(rc/evaluate "z$coefficients")
(rc/evaluate "sum_z$r.squared")

;; get results into local vectors
;;
(vec (.AsDoubles (rc/evaluate "z$coefficients")))
(vec (.AsDoubles (rc/evaluate "sum_z$r.squared")))



;; plot to PDF on server side
;;
(rc/evaluate-remote-only "library(ggplot2)")
(rc/evaluate-remote-only "pdf('Data Plot.pdf')")
(rc/evaluate-remote-only "print(qplot(x,y, data=d))")
(rc/evaluate-remote-only "dev.off()")

;; steps:
;; generate a #<FileStream System.IO.FileStream>
;; (def f (System.IO.File/Create "outfile.pdf"))
;; (.CopyTo (rc/read-file "outfile.pdf") f)
;; (.Dispose f)

;; improved read-file function:
(rc/read-file "Data Plot.pdf")



;;(rc/close)
