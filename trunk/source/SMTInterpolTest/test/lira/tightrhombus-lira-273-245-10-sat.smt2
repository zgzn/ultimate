(set-info :souce |A tight rhombus without solutions.  This benchmark is designed to be hard for cut engines.
Authors: The SMTInterpol team|)
(set-info :status sat)
(set-info :category "crafted")
(set-logic QF_LIRA)
(declare-fun x () Int)
(declare-fun y () Real)
(declare-fun z () Int)
(assert (and
	(<= 0 (- (* 27300000000000 x) (* 24500000000001 y)))
	(<= (- (* 27300000000000 x) (* 24500000000001 y)) 99999999999)
	(<= 1 (- (* 27300000000001 x) (* 24500000000000 y)))
	(<= (- (* 27300000000001 x) (* 24500000000000 y)) 100000000000)))
(assert (<= 0 (- y z)))
(assert (<= (- y z) (/ 600000000009 24500000000000)))
(check-sat)
(exit)
