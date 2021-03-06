//#Safe
type _SIZE_T_TYPE = bv32;

procedure _ATOMIC_OP32(x: [bv32]bv32, y: bv32) returns (z$1: bv32, A$1: [bv32]bv32, z$2: bv32, A$2: [bv32]bv32);

var {:source_name "constant_array"} {:constant} $$constant_array$1: [bv32]bv32;

var {:source_name "constant_array"} {:constant} $$constant_array$2: [bv32]bv32;

axiom {:array_info "$$constant_array"} {:constant} {:elem_width 32} {:source_name "constant_array"} {:source_elem_width 32} {:source_dimensions "*"} true;

var {:source_name "call_value"} {:global} $$call_value: [bv32]bv32;

axiom {:array_info "$$call_value"} {:global} {:elem_width 32} {:source_name "call_value"} {:source_elem_width 32} {:source_dimensions "*"} true;

var {:race_checking} {:global} {:elem_width 32} {:source_elem_width 32} {:source_dimensions "*"} _READ_HAS_OCCURRED_$$call_value: bool;

var {:race_checking} {:global} {:elem_width 32} {:source_elem_width 32} {:source_dimensions "*"} _WRITE_HAS_OCCURRED_$$call_value: bool;

var {:race_checking} {:global} {:elem_width 32} {:source_elem_width 32} {:source_dimensions "*"} _ATOMIC_HAS_OCCURRED_$$call_value: bool;

var {:source_name "call_buffer"} {:global} $$call_buffer: [bv32]bv32;

axiom {:array_info "$$call_buffer"} {:global} {:elem_width 32} {:source_name "call_buffer"} {:source_elem_width 32} {:source_dimensions "*"} true;

var {:race_checking} {:global} {:elem_width 32} {:source_elem_width 32} {:source_dimensions "*"} _READ_HAS_OCCURRED_$$call_buffer: bool;

var {:race_checking} {:global} {:elem_width 32} {:source_elem_width 32} {:source_dimensions "*"} _WRITE_HAS_OCCURRED_$$call_buffer: bool;

var {:race_checking} {:global} {:elem_width 32} {:source_elem_width 32} {:source_dimensions "*"} _ATOMIC_HAS_OCCURRED_$$call_buffer: bool;

var {:source_name "call_a"} {:group_shared} $$binomial_options_kernel.call_a: [bv1][bv32]bv32;

axiom {:array_info "$$binomial_options_kernel.call_a"} {:group_shared} {:elem_width 32} {:source_name "call_a"} {:source_elem_width 32} {:source_dimensions "257"} true;

var {:race_checking} {:group_shared} {:elem_width 32} {:source_elem_width 32} {:source_dimensions "*"} _READ_HAS_OCCURRED_$$binomial_options_kernel.call_a: bool;

var {:race_checking} {:group_shared} {:elem_width 32} {:source_elem_width 32} {:source_dimensions "*"} _WRITE_HAS_OCCURRED_$$binomial_options_kernel.call_a: bool;

var {:race_checking} {:group_shared} {:elem_width 32} {:source_elem_width 32} {:source_dimensions "*"} _ATOMIC_HAS_OCCURRED_$$binomial_options_kernel.call_a: bool;

var {:source_name "call_b"} {:group_shared} $$binomial_options_kernel.call_b: [bv1][bv32]bv32;

axiom {:array_info "$$binomial_options_kernel.call_b"} {:group_shared} {:elem_width 32} {:source_name "call_b"} {:source_elem_width 32} {:source_dimensions "257"} true;

var {:race_checking} {:group_shared} {:elem_width 32} {:source_elem_width 32} {:source_dimensions "*"} _READ_HAS_OCCURRED_$$binomial_options_kernel.call_b: bool;

var {:race_checking} {:group_shared} {:elem_width 32} {:source_elem_width 32} {:source_dimensions "*"} _WRITE_HAS_OCCURRED_$$binomial_options_kernel.call_b: bool;

var {:race_checking} {:group_shared} {:elem_width 32} {:source_elem_width 32} {:source_dimensions "*"} _ATOMIC_HAS_OCCURRED_$$binomial_options_kernel.call_b: bool;

const _WATCHED_OFFSET: bv32;

const {:global_offset_x} global_offset_x: bv32;

const {:global_offset_y} global_offset_y: bv32;

const {:global_offset_z} global_offset_z: bv32;

const {:group_id_x} group_id_x$1: bv32;

const {:group_id_x} group_id_x$2: bv32;

const {:group_size_x} group_size_x: bv32;

const {:group_size_y} group_size_y: bv32;

const {:group_size_z} group_size_z: bv32;

const {:local_id_x} local_id_x$1: bv32;

const {:local_id_x} local_id_x$2: bv32;

const {:num_groups_x} num_groups_x: bv32;

const {:num_groups_y} num_groups_y: bv32;

const {:num_groups_z} num_groups_z: bv32;

function FADD32(bv32, bv32) : bv32;

function FMUL32(bv32, bv32) : bv32;

function {:builtin "bvadd"} BV32_ADD(bv32, bv32) : bv32;

function {:builtin "bvmul"} BV32_MUL(bv32, bv32) : bv32;

function {:builtin "bvsge"} BV32_SGE(bv32, bv32) : bool;

function {:builtin "bvsgt"} BV32_SGT(bv32, bv32) : bool;

function {:builtin "bvsle"} BV32_SLE(bv32, bv32) : bool;

function {:builtin "bvslt"} BV32_SLT(bv32, bv32) : bool;

function {:builtin "bvsub"} BV32_SUB(bv32, bv32) : bv32;

procedure {:source_name "expiry_call_value"} $expiry_call_value(_P$1: bool, $s$1: bv32, $x$1: bv32, $vdt$1: bv32, $t$1: bv32, _P$2: bool, $s$2: bv32, $x$2: bv32, $vdt$2: bv32, $t$2: bv32) returns ($ret$1: bv32, $ret$2: bv32);
  requires _READ_HAS_OCCURRED_$$binomial_options_kernel.call_a ==> group_id_x$1 == group_id_x$2 && group_id_y$1 == group_id_y$2 && group_id_z$1 == group_id_z$2;
  requires _WRITE_HAS_OCCURRED_$$binomial_options_kernel.call_a ==> group_id_x$1 == group_id_x$2 && group_id_y$1 == group_id_y$2 && group_id_z$1 == group_id_z$2;
  requires _ATOMIC_HAS_OCCURRED_$$binomial_options_kernel.call_a ==> group_id_x$1 == group_id_x$2 && group_id_y$1 == group_id_y$2 && group_id_z$1 == group_id_z$2;
  requires _READ_HAS_OCCURRED_$$binomial_options_kernel.call_b ==> group_id_x$1 == group_id_x$2 && group_id_y$1 == group_id_y$2 && group_id_z$1 == group_id_z$2;
  requires _WRITE_HAS_OCCURRED_$$binomial_options_kernel.call_b ==> group_id_x$1 == group_id_x$2 && group_id_y$1 == group_id_y$2 && group_id_z$1 == group_id_z$2;
  requires _ATOMIC_HAS_OCCURRED_$$binomial_options_kernel.call_b ==> group_id_x$1 == group_id_x$2 && group_id_y$1 == group_id_y$2 && group_id_z$1 == group_id_z$2;
  requires BV32_SGT(group_size_x, 0bv32);
  requires BV32_SGT(num_groups_x, 0bv32);
  requires BV32_SGE(group_id_x$1, 0bv32);
  requires BV32_SGE(group_id_x$2, 0bv32);
  requires BV32_SLT(group_id_x$1, num_groups_x);
  requires BV32_SLT(group_id_x$2, num_groups_x);
  requires BV32_SGE(local_id_x$1, 0bv32);
  requires BV32_SGE(local_id_x$2, 0bv32);
  requires BV32_SLT(local_id_x$1, group_size_x);
  requires BV32_SLT(local_id_x$2, group_size_x);
  requires BV32_SGT(group_size_y, 0bv32);
  requires BV32_SGT(num_groups_y, 0bv32);
  requires BV32_SGE(group_id_y$1, 0bv32);
  requires BV32_SGE(group_id_y$2, 0bv32);
  requires BV32_SLT(group_id_y$1, num_groups_y);
  requires BV32_SLT(group_id_y$2, num_groups_y);
  requires BV32_SGE(local_id_y$1, 0bv32);
  requires BV32_SGE(local_id_y$2, 0bv32);
  requires BV32_SLT(local_id_y$1, group_size_y);
  requires BV32_SLT(local_id_y$2, group_size_y);
  requires BV32_SGT(group_size_z, 0bv32);
  requires BV32_SGT(num_groups_z, 0bv32);
  requires BV32_SGE(group_id_z$1, 0bv32);
  requires BV32_SGE(group_id_z$2, 0bv32);
  requires BV32_SLT(group_id_z$1, num_groups_z);
  requires BV32_SLT(group_id_z$2, num_groups_z);
  requires BV32_SGE(local_id_z$1, 0bv32);
  requires BV32_SGE(local_id_z$2, 0bv32);
  requires BV32_SLT(local_id_z$1, group_size_z);
  requires BV32_SLT(local_id_z$2, group_size_z);
  requires group_id_x$1 == group_id_x$2 && group_id_y$1 == group_id_y$2 && group_id_z$1 == group_id_z$2 ==> local_id_x$1 != local_id_x$2 || local_id_y$1 != local_id_y$2 || local_id_z$1 != local_id_z$2;
  ensures _READ_HAS_OCCURRED_$$binomial_options_kernel.call_a ==> group_id_x$1 == group_id_x$2 && group_id_y$1 == group_id_y$2 && group_id_z$1 == group_id_z$2;
  ensures _WRITE_HAS_OCCURRED_$$binomial_options_kernel.call_a ==> group_id_x$1 == group_id_x$2 && group_id_y$1 == group_id_y$2 && group_id_z$1 == group_id_z$2;
  ensures _ATOMIC_HAS_OCCURRED_$$binomial_options_kernel.call_a ==> group_id_x$1 == group_id_x$2 && group_id_y$1 == group_id_y$2 && group_id_z$1 == group_id_z$2;
  ensures _READ_HAS_OCCURRED_$$binomial_options_kernel.call_b ==> group_id_x$1 == group_id_x$2 && group_id_y$1 == group_id_y$2 && group_id_z$1 == group_id_z$2;
  ensures _WRITE_HAS_OCCURRED_$$binomial_options_kernel.call_b ==> group_id_x$1 == group_id_x$2 && group_id_y$1 == group_id_y$2 && group_id_z$1 == group_id_z$2;
  ensures _ATOMIC_HAS_OCCURRED_$$binomial_options_kernel.call_b ==> group_id_x$1 == group_id_x$2 && group_id_y$1 == group_id_y$2 && group_id_z$1 == group_id_z$2;

implementation {:source_name "expiry_call_value"} $expiry_call_value(_P$1: bool, $s$1: bv32, $x$1: bv32, $vdt$1: bv32, $t$1: bv32, _P$2: bool, $s$2: bv32, $x$2: bv32, $vdt$2: bv32, $t$2: bv32) returns ($ret$1: bv32, $ret$2: bv32)
{

  $entry:
    $ret$1 := (if _P$1 then 0bv32 else $ret$1);
    $ret$2 := (if _P$2 then 0bv32 else $ret$2);
    return;
}

procedure {:source_name "binomial_options_kernel"} ULTIMATE.start();
  requires !_READ_HAS_OCCURRED_$$call_value && !_WRITE_HAS_OCCURRED_$$call_value && !_ATOMIC_HAS_OCCURRED_$$call_value;
  requires !_READ_HAS_OCCURRED_$$call_buffer && !_WRITE_HAS_OCCURRED_$$call_buffer && !_ATOMIC_HAS_OCCURRED_$$call_buffer;
  requires !_READ_HAS_OCCURRED_$$binomial_options_kernel.call_a && !_WRITE_HAS_OCCURRED_$$binomial_options_kernel.call_a && !_ATOMIC_HAS_OCCURRED_$$binomial_options_kernel.call_a;
  requires !_READ_HAS_OCCURRED_$$binomial_options_kernel.call_b && !_WRITE_HAS_OCCURRED_$$binomial_options_kernel.call_b && !_ATOMIC_HAS_OCCURRED_$$binomial_options_kernel.call_b;
  requires BV32_SGT(group_size_x, 0bv32);
  requires BV32_SGT(num_groups_x, 0bv32);
  requires BV32_SGE(group_id_x$1, 0bv32);
  requires BV32_SGE(group_id_x$2, 0bv32);
  requires BV32_SLT(group_id_x$1, num_groups_x);
  requires BV32_SLT(group_id_x$2, num_groups_x);
  requires BV32_SGE(local_id_x$1, 0bv32);
  requires BV32_SGE(local_id_x$2, 0bv32);
  requires BV32_SLT(local_id_x$1, group_size_x);
  requires BV32_SLT(local_id_x$2, group_size_x);
  requires BV32_SGT(group_size_y, 0bv32);
  requires BV32_SGT(num_groups_y, 0bv32);
  requires BV32_SGE(group_id_y$1, 0bv32);
  requires BV32_SGE(group_id_y$2, 0bv32);
  requires BV32_SLT(group_id_y$1, num_groups_y);
  requires BV32_SLT(group_id_y$2, num_groups_y);
  requires BV32_SGE(local_id_y$1, 0bv32);
  requires BV32_SGE(local_id_y$2, 0bv32);
  requires BV32_SLT(local_id_y$1, group_size_y);
  requires BV32_SLT(local_id_y$2, group_size_y);
  requires BV32_SGT(group_size_z, 0bv32);
  requires BV32_SGT(num_groups_z, 0bv32);
  requires BV32_SGE(group_id_z$1, 0bv32);
  requires BV32_SGE(group_id_z$2, 0bv32);
  requires BV32_SLT(group_id_z$1, num_groups_z);
  requires BV32_SLT(group_id_z$2, num_groups_z);
  requires BV32_SGE(local_id_z$1, 0bv32);
  requires BV32_SGE(local_id_z$2, 0bv32);
  requires BV32_SLT(local_id_z$1, group_size_z);
  requires BV32_SLT(local_id_z$2, group_size_z);
  requires group_id_x$1 == group_id_x$2 && group_id_y$1 == group_id_y$2 && group_id_z$1 == group_id_z$2 ==> local_id_x$1 != local_id_x$2 || local_id_y$1 != local_id_y$2 || local_id_z$1 != local_id_z$2;
  modifies $$binomial_options_kernel.call_a, $$binomial_options_kernel.call_b, _WRITE_HAS_OCCURRED_$$call_buffer, _WRITE_READ_BENIGN_FLAG_$$call_buffer, _WRITE_READ_BENIGN_FLAG_$$call_buffer, _READ_HAS_OCCURRED_$$binomial_options_kernel.call_a, _WRITE_HAS_OCCURRED_$$call_value, _WRITE_READ_BENIGN_FLAG_$$call_value, _WRITE_READ_BENIGN_FLAG_$$call_value, $$call_value, $$call_buffer, _TRACKING, _READ_HAS_OCCURRED_$$call_buffer, _WRITE_HAS_OCCURRED_$$binomial_options_kernel.call_a, _WRITE_READ_BENIGN_FLAG_$$binomial_options_kernel.call_a, _WRITE_READ_BENIGN_FLAG_$$binomial_options_kernel.call_a, _TRACKING, _TRACKING, _WRITE_HAS_OCCURRED_$$binomial_options_kernel.call_b, _WRITE_READ_BENIGN_FLAG_$$binomial_options_kernel.call_b, _WRITE_READ_BENIGN_FLAG_$$binomial_options_kernel.call_b, _TRACKING, _READ_HAS_OCCURRED_$$binomial_options_kernel.call_b;

implementation {:source_name "binomial_options_kernel"} ULTIMATE.start()
{
  var $i.0$1: bv32;
  var $i.0$2: bv32;
  var $i.1: bv32;
  var $c_base.0: bv32;
  var $c_start.0: bv32;
  var $k.0: bv32;
  var v0$1: bv32;
  var v0$2: bv32;
  var v1$1: bv32;
  var v1$2: bv32;
  var v2$1: bool;
  var v2$2: bool;
  var v3$1: bv32;
  var v3$2: bv32;
  var v4$1: bv32;
  var v4$2: bv32;
  var v5$1: bv32;
  var v5$2: bv32;
  var v6$1: bv32;
  var v6$2: bv32;
  var v7: bool;
  var v8: bool;
  var v9: bool;
  var v10: bv32;
  var v11$1: bool;
  var v11$2: bool;
  var v12$1: bv32;
  var v12$2: bv32;
  var v13: bool;
  var v14$1: bv32;
  var v14$2: bv32;
  var v15$1: bv32;
  var v15$2: bv32;
  var v16$1: bv32;
  var v16$2: bv32;
  var v17$1: bv32;
  var v17$2: bv32;
  var v18$1: bv32;
  var v18$2: bv32;
  var v19$1: bv32;
  var v19$2: bv32;
  var v20$1: bv32;
  var v20$2: bv32;
  var v21$1: bv32;
  var v21$2: bv32;
  var v22$1: bool;
  var v22$2: bool;
  var v23$1: bv32;
  var v23$2: bv32;
  var v24$1: bool;
  var v24$2: bool;
  var v25$1: bv32;
  var v25$2: bv32;
  var p0$1: bool;
  var p0$2: bool;
  var p1$1: bool;
  var p1$2: bool;
  var p2$1: bool;
  var p2$2: bool;
  var p3$1: bool;
  var p3$2: bool;
  var p4$1: bool;
  var p4$2: bool;
  var p5$1: bool;
  var p5$2: bool;
  var p6$1: bool;
  var p6$2: bool;
  var p7$1: bool;
  var p7$2: bool;
  var p8$1: bool;
  var p8$2: bool;

  $entry:
    v0$1 := group_id_x$1;
    v0$2 := group_id_x$2;
    v1$1 := local_id_x$1;
    v1$2 := local_id_x$2;
    $i.0$1 := v1$1;
    $i.0$2 := v1$2;
    p0$1 := false;
    p0$2 := false;
    p0$1 := true;
    p0$2 := true;
    assume {:captureState "loop_entry_state_3_0"} true;
    goto $for.cond;

  $for.cond:
    assume {:captureState "loop_head_state_3"} true;
    assert {:tag "groupSharedArraysDisjointAcrossGroups"} _ATOMIC_HAS_OCCURRED_$$binomial_options_kernel.call_b ==> group_id_x$1 == group_id_x$2 && group_id_y$1 == group_id_y$2 && group_id_z$1 == group_id_z$2;
    assert {:tag "groupSharedArraysDisjointAcrossGroups"} _WRITE_HAS_OCCURRED_$$binomial_options_kernel.call_b ==> group_id_x$1 == group_id_x$2 && group_id_y$1 == group_id_y$2 && group_id_z$1 == group_id_z$2;
    assert {:tag "groupSharedArraysDisjointAcrossGroups"} _READ_HAS_OCCURRED_$$binomial_options_kernel.call_b ==> group_id_x$1 == group_id_x$2 && group_id_y$1 == group_id_y$2 && group_id_z$1 == group_id_z$2;
    assert {:tag "groupSharedArraysDisjointAcrossGroups"} _ATOMIC_HAS_OCCURRED_$$binomial_options_kernel.call_a ==> group_id_x$1 == group_id_x$2 && group_id_y$1 == group_id_y$2 && group_id_z$1 == group_id_z$2;
    assert {:tag "groupSharedArraysDisjointAcrossGroups"} _WRITE_HAS_OCCURRED_$$binomial_options_kernel.call_a ==> group_id_x$1 == group_id_x$2 && group_id_y$1 == group_id_y$2 && group_id_z$1 == group_id_z$2;
    assert {:tag "groupSharedArraysDisjointAcrossGroups"} _READ_HAS_OCCURRED_$$binomial_options_kernel.call_a ==> group_id_x$1 == group_id_x$2 && group_id_y$1 == group_id_y$2 && group_id_z$1 == group_id_z$2;
    assert {:block_sourceloc} {:sourceloc_num 2} p0$1 ==> true;
    v2$1 := (if p0$1 then BV32_SLE($i.0$1, 2048bv32) else v2$1);
    v2$2 := (if p0$2 then BV32_SLE($i.0$2, 2048bv32) else v2$2);
    p1$1 := false;
    p1$2 := false;
    p2$1 := false;
    p2$2 := false;
    p1$1 := (if p0$1 && v2$1 then v2$1 else p1$1);
    p1$2 := (if p0$2 && v2$2 then v2$2 else p1$2);
    p0$1 := (if p0$1 && !v2$1 then v2$1 else p0$1);
    p0$2 := (if p0$2 && !v2$2 then v2$2 else p0$2);
    v3$1 := (if p1$1 then $$constant_array$1[v0$1] else v3$1);
    v3$2 := (if p1$2 then $$constant_array$2[v0$2] else v3$2);
    v4$1 := (if p1$1 then $$constant_array$1[v0$1] else v4$1);
    v4$2 := (if p1$2 then $$constant_array$2[v0$2] else v4$2);
    v5$1 := (if p1$1 then $$constant_array$1[v0$1] else v5$1);
    v5$2 := (if p1$2 then $$constant_array$2[v0$2] else v5$2);
    call v6$1, v6$2 := $expiry_call_value(p1$1, v3$1, v4$1, v5$1, $i.0$1, p1$2, v3$2, v4$2, v5$2, $i.0$2);
    assume {:captureState "call_return_state_0"} {:procedureName "$expiry_call_value"} true;
    call _LOG_WRITE_$$call_buffer(p1$1, BV32_ADD(BV32_MUL(v0$1, 2064bv32), $i.0$1), v6$1, $$call_buffer[BV32_ADD(BV32_MUL(v0$1, 2064bv32), $i.0$1)]);
    call _UPDATE_WRITE_READ_BENIGN_FLAG_$$call_buffer(p1$2, BV32_ADD(BV32_MUL(v0$2, 2064bv32), $i.0$2));
    assume {:do_not_predicate} {:check_id "check_state_12"} {:captureState "check_state_12"} {:sourceloc} {:sourceloc_num 8} true;
    call _CHECK_WRITE_$$call_buffer(p1$2, BV32_ADD(BV32_MUL(v0$2, 2064bv32), $i.0$2), v6$2);
    assume {:captureState "call_return_state_0"} {:procedureName "_CHECK_WRITE_$$call_buffer"} true;
    $$call_buffer[BV32_ADD(BV32_MUL(v0$1, 2064bv32), $i.0$1)] := (if p1$1 then v6$1 else $$call_buffer[BV32_ADD(BV32_MUL(v0$1, 2064bv32), $i.0$1)]);
    $$call_buffer[BV32_ADD(BV32_MUL(v0$2, 2064bv32), $i.0$2)] := (if p1$2 then v6$2 else $$call_buffer[BV32_ADD(BV32_MUL(v0$2, 2064bv32), $i.0$2)]);
    $i.0$1 := (if p1$1 then BV32_ADD($i.0$1, 256bv32) else $i.0$1);
    $i.0$2 := (if p1$2 then BV32_ADD($i.0$2, 256bv32) else $i.0$2);
    p0$1 := (if p1$1 then true else p0$1);
    p0$2 := (if p1$2 then true else p0$2);
    goto $for.cond.backedge, $for.cond.tail;

  $for.cond.tail:
    assume !p0$1 && !p0$2;
    $i.1 := 2048bv32;
    assume {:captureState "loop_entry_state_0_0"} true;
    goto $for.cond7;

  $for.cond7:
    assume {:captureState "loop_head_state_0"} true;
    assert {:tag "groupSharedArraysDisjointAcrossGroups"} _ATOMIC_HAS_OCCURRED_$$binomial_options_kernel.call_b ==> group_id_x$1 == group_id_x$2 && group_id_y$1 == group_id_y$2 && group_id_z$1 == group_id_z$2;
    assert {:tag "groupSharedArraysDisjointAcrossGroups"} _WRITE_HAS_OCCURRED_$$binomial_options_kernel.call_b ==> group_id_x$1 == group_id_x$2 && group_id_y$1 == group_id_y$2 && group_id_z$1 == group_id_z$2;
    assert {:tag "groupSharedArraysDisjointAcrossGroups"} _READ_HAS_OCCURRED_$$binomial_options_kernel.call_b ==> group_id_x$1 == group_id_x$2 && group_id_y$1 == group_id_y$2 && group_id_z$1 == group_id_z$2;
    assert {:tag "groupSharedArraysDisjointAcrossGroups"} _ATOMIC_HAS_OCCURRED_$$binomial_options_kernel.call_a ==> group_id_x$1 == group_id_x$2 && group_id_y$1 == group_id_y$2 && group_id_z$1 == group_id_z$2;
    assert {:tag "groupSharedArraysDisjointAcrossGroups"} _WRITE_HAS_OCCURRED_$$binomial_options_kernel.call_a ==> group_id_x$1 == group_id_x$2 && group_id_y$1 == group_id_y$2 && group_id_z$1 == group_id_z$2;
    assert {:tag "groupSharedArraysDisjointAcrossGroups"} _READ_HAS_OCCURRED_$$binomial_options_kernel.call_a ==> group_id_x$1 == group_id_x$2 && group_id_y$1 == group_id_y$2 && group_id_z$1 == group_id_z$2;
    assert {:block_sourceloc} {:sourceloc_num 11} true;
    v7 := BV32_SGT($i.1, 0bv32);
    p7$1 := false;
    p7$2 := false;
    p8$1 := false;
    p8$2 := false;
    goto $truebb0, $falsebb0;

  $falsebb0:
    assume {:partition} !v7;
    v24$1 := v1$1 == 0bv32;
    v24$2 := v1$2 == 0bv32;
    p7$1 := (if v24$1 then v24$1 else p7$1);
    p7$2 := (if v24$2 then v24$2 else p7$2);
    call _LOG_READ_$$binomial_options_kernel.call_a(p7$1, 0bv32, $$binomial_options_kernel.call_a[1bv1][0bv32]);
    assume {:do_not_predicate} {:check_id "check_state_0"} {:captureState "check_state_0"} {:sourceloc} {:sourceloc_num 48} true;
    call _CHECK_READ_$$binomial_options_kernel.call_a(p7$2, 0bv32, $$binomial_options_kernel.call_a[(if group_id_x$1 == group_id_x$2 && group_id_y$1 == group_id_y$2 && group_id_z$1 == group_id_z$2 then 1bv1 else 0bv1)][0bv32]);
    assume {:captureState "call_return_state_0"} {:procedureName "_CHECK_READ_$$binomial_options_kernel.call_a"} true;
    v25$1 := (if p7$1 then $$binomial_options_kernel.call_a[1bv1][0bv32] else v25$1);
    v25$2 := (if p7$2 then $$binomial_options_kernel.call_a[(if group_id_x$1 == group_id_x$2 && group_id_y$1 == group_id_y$2 && group_id_z$1 == group_id_z$2 then 1bv1 else 0bv1)][0bv32] else v25$2);
    call _LOG_WRITE_$$call_value(p7$1, v0$1, v25$1, $$call_value[v0$1]);
    call _UPDATE_WRITE_READ_BENIGN_FLAG_$$call_value(p7$2, v0$2);
    assume {:do_not_predicate} {:check_id "check_state_1"} {:captureState "check_state_1"} {:sourceloc} {:sourceloc_num 49} true;
    call _CHECK_WRITE_$$call_value(p7$2, v0$2, v25$2);
    assume {:captureState "call_return_state_0"} {:procedureName "_CHECK_WRITE_$$call_value"} true;
    $$call_value[v0$1] := (if p7$1 then v25$1 else $$call_value[v0$1]);
    $$call_value[v0$2] := (if p7$2 then v25$2 else $$call_value[v0$2]);
    return;

  $truebb0:
    assume {:partition} v7;
    $c_base.0 := 0bv32;
    assume {:captureState "loop_entry_state_1_0"} true;
    goto $for.cond10;

  $for.cond10:
    assume {:captureState "loop_head_state_1"} true;
    assert {:tag "groupSharedArraysDisjointAcrossGroups"} _ATOMIC_HAS_OCCURRED_$$binomial_options_kernel.call_b ==> group_id_x$1 == group_id_x$2 && group_id_y$1 == group_id_y$2 && group_id_z$1 == group_id_z$2;
    assert {:tag "groupSharedArraysDisjointAcrossGroups"} _WRITE_HAS_OCCURRED_$$binomial_options_kernel.call_b ==> group_id_x$1 == group_id_x$2 && group_id_y$1 == group_id_y$2 && group_id_z$1 == group_id_z$2;
    assert {:tag "groupSharedArraysDisjointAcrossGroups"} _READ_HAS_OCCURRED_$$binomial_options_kernel.call_b ==> group_id_x$1 == group_id_x$2 && group_id_y$1 == group_id_y$2 && group_id_z$1 == group_id_z$2;
    assert {:tag "groupSharedArraysDisjointAcrossGroups"} _ATOMIC_HAS_OCCURRED_$$binomial_options_kernel.call_a ==> group_id_x$1 == group_id_x$2 && group_id_y$1 == group_id_y$2 && group_id_z$1 == group_id_z$2;
    assert {:tag "groupSharedArraysDisjointAcrossGroups"} _WRITE_HAS_OCCURRED_$$binomial_options_kernel.call_a ==> group_id_x$1 == group_id_x$2 && group_id_y$1 == group_id_y$2 && group_id_z$1 == group_id_z$2;
    assert {:tag "groupSharedArraysDisjointAcrossGroups"} _READ_HAS_OCCURRED_$$binomial_options_kernel.call_a ==> group_id_x$1 == group_id_x$2 && group_id_y$1 == group_id_y$2 && group_id_z$1 == group_id_z$2;
    assert {:block_sourceloc} {:sourceloc_num 13} true;
    v8 := BV32_SLT($c_base.0, $i.1);
    p3$1 := false;
    p3$2 := false;
    p4$1 := false;
    p4$2 := false;
    goto $truebb1, $falsebb1;

  $falsebb1:
    assume {:partition} !v8;
    $i.1 := BV32_SUB($i.1, 32bv32);
    assume {:captureState "loop_back_edge_state_0_0"} true;
    goto $for.cond7;

  $truebb1:
    assume {:partition} v8;
    v9 := BV32_SLT(255bv32, BV32_SUB($i.1, $c_base.0));
    goto $truebb2, $falsebb2;

  $falsebb2:
    assume {:partition} !v9;
    $c_start.0 := BV32_SUB($i.1, $c_base.0);
    goto __partitioned_block_$if.end_0;

  __partitioned_block_$if.end_0:
    v10 := BV32_SUB($c_start.0, 32bv32);
    goto __partitioned_block_$if.end_1;

  __partitioned_block_$if.end_1:
    call $bugle_barrier_duplicated_0(1bv1, 1bv1);
    v11$1 := BV32_SLE(v1$1, $c_start.0);
    v11$2 := BV32_SLE(v1$2, $c_start.0);
    p4$1 := (if v11$1 then v11$1 else p4$1);
    p4$2 := (if v11$2 then v11$2 else p4$2);
    call _LOG_READ_$$call_buffer(p4$1, BV32_ADD(BV32_MUL(v0$1, 2064bv32), BV32_ADD($c_base.0, v1$1)), $$call_buffer[BV32_ADD(BV32_MUL(v0$1, 2064bv32), BV32_ADD($c_base.0, v1$1))]);
    assume {:do_not_predicate} {:check_id "check_state_10"} {:captureState "check_state_10"} {:sourceloc} {:sourceloc_num 20} true;
    call _CHECK_READ_$$call_buffer(p4$2, BV32_ADD(BV32_MUL(v0$2, 2064bv32), BV32_ADD($c_base.0, v1$2)), $$call_buffer[BV32_ADD(BV32_MUL(v0$2, 2064bv32), BV32_ADD($c_base.0, v1$2))]);
    assume {:captureState "call_return_state_0"} {:procedureName "_CHECK_READ_$$call_buffer"} true;
    v12$1 := (if p4$1 then $$call_buffer[BV32_ADD(BV32_MUL(v0$1, 2064bv32), BV32_ADD($c_base.0, v1$1))] else v12$1);
    v12$2 := (if p4$2 then $$call_buffer[BV32_ADD(BV32_MUL(v0$2, 2064bv32), BV32_ADD($c_base.0, v1$2))] else v12$2);
    call _LOG_WRITE_$$binomial_options_kernel.call_a(p4$1, v1$1, v12$1, $$binomial_options_kernel.call_a[1bv1][v1$1]);
    call _UPDATE_WRITE_READ_BENIGN_FLAG_$$binomial_options_kernel.call_a(p4$2, v1$2);
    assume {:do_not_predicate} {:check_id "check_state_11"} {:captureState "check_state_11"} {:sourceloc} {:sourceloc_num 21} true;
    call _CHECK_WRITE_$$binomial_options_kernel.call_a(p4$2, v1$2, v12$2);
    assume {:captureState "call_return_state_0"} {:procedureName "_CHECK_WRITE_$$binomial_options_kernel.call_a"} true;
    $$binomial_options_kernel.call_a[1bv1][v1$1] := (if p4$1 then v12$1 else $$binomial_options_kernel.call_a[1bv1][v1$1]);
    $$binomial_options_kernel.call_a[(if group_id_x$1 == group_id_x$2 && group_id_y$1 == group_id_y$2 && group_id_z$1 == group_id_z$2 then 1bv1 else 0bv1)][v1$2] := (if p4$2 then v12$2 else $$binomial_options_kernel.call_a[(if group_id_x$1 == group_id_x$2 && group_id_y$1 == group_id_y$2 && group_id_z$1 == group_id_z$2 then 1bv1 else 0bv1)][v1$2]);
    $k.0 := BV32_SUB($c_start.0, 1bv32);
    assume {:captureState "loop_entry_state_2_0"} true;
    goto $for.cond25;

  $for.cond25:
    assume {:captureState "loop_head_state_2"} true;
    assert {:tag "groupSharedArraysDisjointAcrossGroups"} _ATOMIC_HAS_OCCURRED_$$binomial_options_kernel.call_b ==> group_id_x$1 == group_id_x$2 && group_id_y$1 == group_id_y$2 && group_id_z$1 == group_id_z$2;
    assert {:tag "groupSharedArraysDisjointAcrossGroups"} _WRITE_HAS_OCCURRED_$$binomial_options_kernel.call_b ==> group_id_x$1 == group_id_x$2 && group_id_y$1 == group_id_y$2 && group_id_z$1 == group_id_z$2;
    assert {:tag "groupSharedArraysDisjointAcrossGroups"} _READ_HAS_OCCURRED_$$binomial_options_kernel.call_b ==> group_id_x$1 == group_id_x$2 && group_id_y$1 == group_id_y$2 && group_id_z$1 == group_id_z$2;
    assert {:tag "groupSharedArraysDisjointAcrossGroups"} _ATOMIC_HAS_OCCURRED_$$binomial_options_kernel.call_a ==> group_id_x$1 == group_id_x$2 && group_id_y$1 == group_id_y$2 && group_id_z$1 == group_id_z$2;
    assert {:tag "groupSharedArraysDisjointAcrossGroups"} _WRITE_HAS_OCCURRED_$$binomial_options_kernel.call_a ==> group_id_x$1 == group_id_x$2 && group_id_y$1 == group_id_y$2 && group_id_z$1 == group_id_z$2;
    assert {:tag "groupSharedArraysDisjointAcrossGroups"} _READ_HAS_OCCURRED_$$binomial_options_kernel.call_a ==> group_id_x$1 == group_id_x$2 && group_id_y$1 == group_id_y$2 && group_id_z$1 == group_id_z$2;
    assert {:block_sourceloc} {:sourceloc_num 23} true;
    v13 := BV32_SGE($k.0, v10);
    p5$1 := false;
    p5$2 := false;
    p6$1 := false;
    p6$2 := false;
    goto __partitioned_block_$truebb4_0, __partitioned_block_$falsebb4_0;

  __partitioned_block_$falsebb4_0:
    assume {:partition} !v13;
    goto __partitioned_block_$falsebb4_1;

  __partitioned_block_$falsebb4_1:
    call $bugle_barrier_duplicated_1(1bv1, 1bv1);
    v22$1 := BV32_SLE(v1$1, v10);
    v22$2 := BV32_SLE(v1$2, v10);
    p6$1 := (if v22$1 then v22$1 else p6$1);
    p6$2 := (if v22$2 then v22$2 else p6$2);
    call _LOG_READ_$$binomial_options_kernel.call_a(p6$1, v1$1, $$binomial_options_kernel.call_a[1bv1][v1$1]);
    assume {:do_not_predicate} {:check_id "check_state_2"} {:captureState "check_state_2"} {:sourceloc} {:sourceloc_num 40} true;
    call _CHECK_READ_$$binomial_options_kernel.call_a(p6$2, v1$2, $$binomial_options_kernel.call_a[(if group_id_x$1 == group_id_x$2 && group_id_y$1 == group_id_y$2 && group_id_z$1 == group_id_z$2 then 1bv1 else 0bv1)][v1$2]);
    assume {:captureState "call_return_state_0"} {:procedureName "_CHECK_READ_$$binomial_options_kernel.call_a"} true;
    v23$1 := (if p6$1 then $$binomial_options_kernel.call_a[1bv1][v1$1] else v23$1);
    v23$2 := (if p6$2 then $$binomial_options_kernel.call_a[(if group_id_x$1 == group_id_x$2 && group_id_y$1 == group_id_y$2 && group_id_z$1 == group_id_z$2 then 1bv1 else 0bv1)][v1$2] else v23$2);
    call _LOG_WRITE_$$call_buffer(p6$1, BV32_ADD(BV32_MUL(v0$1, 2064bv32), BV32_ADD($c_base.0, v1$1)), v23$1, $$call_buffer[BV32_ADD(BV32_MUL(v0$1, 2064bv32), BV32_ADD($c_base.0, v1$1))]);
    call _UPDATE_WRITE_READ_BENIGN_FLAG_$$call_buffer(p6$2, BV32_ADD(BV32_MUL(v0$2, 2064bv32), BV32_ADD($c_base.0, v1$2)));
    assume {:do_not_predicate} {:check_id "check_state_3"} {:captureState "check_state_3"} {:sourceloc} {:sourceloc_num 41} true;
    call _CHECK_WRITE_$$call_buffer(p6$2, BV32_ADD(BV32_MUL(v0$2, 2064bv32), BV32_ADD($c_base.0, v1$2)), v23$2);
    assume {:captureState "call_return_state_0"} {:procedureName "_CHECK_WRITE_$$call_buffer"} true;
    $$call_buffer[BV32_ADD(BV32_MUL(v0$1, 2064bv32), BV32_ADD($c_base.0, v1$1))] := (if p6$1 then v23$1 else $$call_buffer[BV32_ADD(BV32_MUL(v0$1, 2064bv32), BV32_ADD($c_base.0, v1$1))]);
    $$call_buffer[BV32_ADD(BV32_MUL(v0$2, 2064bv32), BV32_ADD($c_base.0, v1$2))] := (if p6$2 then v23$2 else $$call_buffer[BV32_ADD(BV32_MUL(v0$2, 2064bv32), BV32_ADD($c_base.0, v1$2))]);
    $c_base.0 := BV32_ADD($c_base.0, 224bv32);
    assume {:captureState "loop_back_edge_state_1_0"} true;
    goto $for.cond10;

  __partitioned_block_$truebb4_0:
    assume {:partition} v13;
    goto __partitioned_block_$truebb4_1;

  __partitioned_block_$truebb4_1:
    call $bugle_barrier_duplicated_2(1bv1, 1bv1);
    v14$1 := $$constant_array$1[v0$1];
    v14$2 := $$constant_array$2[v0$2];
    assume {:do_not_predicate} {:check_id "check_state_4"} {:captureState "check_state_4"} {:sourceloc} {:sourceloc_num 27} true;
    v15$1 := $$binomial_options_kernel.call_a[1bv1][BV32_ADD(v1$1, 1bv32)];
    v15$2 := $$binomial_options_kernel.call_a[(if group_id_x$1 == group_id_x$2 && group_id_y$1 == group_id_y$2 && group_id_z$1 == group_id_z$2 then 1bv1 else 0bv1)][BV32_ADD(v1$2, 1bv32)];
    v16$1 := $$constant_array$1[v0$1];
    v16$2 := $$constant_array$2[v0$2];
    assume {:do_not_predicate} {:check_id "check_state_5"} {:captureState "check_state_5"} {:sourceloc} {:sourceloc_num 29} true;
    v17$1 := $$binomial_options_kernel.call_a[1bv1][v1$1];
    v17$2 := $$binomial_options_kernel.call_a[(if group_id_x$1 == group_id_x$2 && group_id_y$1 == group_id_y$2 && group_id_z$1 == group_id_z$2 then 1bv1 else 0bv1)][v1$2];
    call _LOG_WRITE_$$binomial_options_kernel.call_b(true, v1$1, FADD32(FMUL32(v14$1, v15$1), FMUL32(v16$1, v17$1)), $$binomial_options_kernel.call_b[1bv1][v1$1]);
    call _UPDATE_WRITE_READ_BENIGN_FLAG_$$binomial_options_kernel.call_b(true, v1$2);
    assume {:do_not_predicate} {:check_id "check_state_6"} {:captureState "check_state_6"} {:sourceloc} {:sourceloc_num 30} true;
    call _CHECK_WRITE_$$binomial_options_kernel.call_b(true, v1$2, FADD32(FMUL32(v14$2, v15$2), FMUL32(v16$2, v17$2)));
    assume {:captureState "call_return_state_0"} {:procedureName "_CHECK_WRITE_$$binomial_options_kernel.call_b"} true;
    $$binomial_options_kernel.call_b[1bv1][v1$1] := FADD32(FMUL32(v14$1, v15$1), FMUL32(v16$1, v17$1));
    $$binomial_options_kernel.call_b[(if group_id_x$1 == group_id_x$2 && group_id_y$1 == group_id_y$2 && group_id_z$1 == group_id_z$2 then 1bv1 else 0bv1)][v1$2] := FADD32(FMUL32(v14$2, v15$2), FMUL32(v16$2, v17$2));
    goto __partitioned_block_$truebb4_2;

  __partitioned_block_$truebb4_2:
    call $bugle_barrier_duplicated_3(1bv1, 1bv1);
    v18$1 := $$constant_array$1[v0$1];
    v18$2 := $$constant_array$2[v0$2];
    call _LOG_READ_$$binomial_options_kernel.call_b(true, BV32_ADD(v1$1, 1bv32), $$binomial_options_kernel.call_b[1bv1][BV32_ADD(v1$1, 1bv32)]);
    assume {:do_not_predicate} {:check_id "check_state_7"} {:captureState "check_state_7"} {:sourceloc} {:sourceloc_num 33} true;
    call _CHECK_READ_$$binomial_options_kernel.call_b(true, BV32_ADD(v1$2, 1bv32), $$binomial_options_kernel.call_b[(if group_id_x$1 == group_id_x$2 && group_id_y$1 == group_id_y$2 && group_id_z$1 == group_id_z$2 then 1bv1 else 0bv1)][BV32_ADD(v1$2, 1bv32)]);
    assume {:captureState "call_return_state_0"} {:procedureName "_CHECK_READ_$$binomial_options_kernel.call_b"} true;
    v19$1 := $$binomial_options_kernel.call_b[1bv1][BV32_ADD(v1$1, 1bv32)];
    v19$2 := $$binomial_options_kernel.call_b[(if group_id_x$1 == group_id_x$2 && group_id_y$1 == group_id_y$2 && group_id_z$1 == group_id_z$2 then 1bv1 else 0bv1)][BV32_ADD(v1$2, 1bv32)];
    v20$1 := $$constant_array$1[v0$1];
    v20$2 := $$constant_array$2[v0$2];
    call _LOG_READ_$$binomial_options_kernel.call_b(true, v1$1, $$binomial_options_kernel.call_b[1bv1][v1$1]);
    assume {:do_not_predicate} {:check_id "check_state_8"} {:captureState "check_state_8"} {:sourceloc} {:sourceloc_num 35} true;
    call _CHECK_READ_$$binomial_options_kernel.call_b(true, v1$2, $$binomial_options_kernel.call_b[(if group_id_x$1 == group_id_x$2 && group_id_y$1 == group_id_y$2 && group_id_z$1 == group_id_z$2 then 1bv1 else 0bv1)][v1$2]);
    assume {:captureState "call_return_state_0"} {:procedureName "_CHECK_READ_$$binomial_options_kernel.call_b"} true;
    v21$1 := $$binomial_options_kernel.call_b[1bv1][v1$1];
    v21$2 := $$binomial_options_kernel.call_b[(if group_id_x$1 == group_id_x$2 && group_id_y$1 == group_id_y$2 && group_id_z$1 == group_id_z$2 then 1bv1 else 0bv1)][v1$2];
    call _LOG_WRITE_$$binomial_options_kernel.call_a(true, v1$1, FADD32(FMUL32(v18$1, v19$1), FMUL32(v20$1, v21$1)), $$binomial_options_kernel.call_a[1bv1][v1$1]);
    call _UPDATE_WRITE_READ_BENIGN_FLAG_$$binomial_options_kernel.call_a(true, v1$2);
    assume {:do_not_predicate} {:check_id "check_state_9"} {:captureState "check_state_9"} {:sourceloc} {:sourceloc_num 36} true;
    call _CHECK_WRITE_$$binomial_options_kernel.call_a(true, v1$2, FADD32(FMUL32(v18$2, v19$2), FMUL32(v20$2, v21$2)));
    assume {:captureState "call_return_state_0"} {:procedureName "_CHECK_WRITE_$$binomial_options_kernel.call_a"} true;
    $$binomial_options_kernel.call_a[1bv1][v1$1] := FADD32(FMUL32(v18$1, v19$1), FMUL32(v20$1, v21$1));
    $$binomial_options_kernel.call_a[(if group_id_x$1 == group_id_x$2 && group_id_y$1 == group_id_y$2 && group_id_z$1 == group_id_z$2 then 1bv1 else 0bv1)][v1$2] := FADD32(FMUL32(v18$2, v19$2), FMUL32(v20$2, v21$2));
    $k.0 := BV32_ADD($k.0, 4294967294bv32);
    assume {:captureState "loop_back_edge_state_2_0"} true;
    goto $for.cond25;

  $truebb2:
    assume {:partition} v9;
    $c_start.0 := 255bv32;
    goto __partitioned_block_$if.end_0;

  $for.cond.backedge:
    assume {:backedge} p0$1 || p0$2;
    assume {:captureState "loop_back_edge_state_3_0"} true;
    goto $for.cond;
}

axiom (if group_size_y == 1bv32 then 1bv1 else 0bv1) != 0bv1;

axiom (if group_size_z == 1bv32 then 1bv1 else 0bv1) != 0bv1;

axiom (if num_groups_y == 1bv32 then 1bv1 else 0bv1) != 0bv1;

axiom (if num_groups_z == 1bv32 then 1bv1 else 0bv1) != 0bv1;

axiom (if group_size_x == 16bv32 then 1bv1 else 0bv1) != 0bv1;

axiom (if num_groups_x == 1bv32 then 1bv1 else 0bv1) != 0bv1;

axiom (if global_offset_x == 0bv32 then 1bv1 else 0bv1) != 0bv1;

axiom (if global_offset_y == 0bv32 then 1bv1 else 0bv1) != 0bv1;

axiom (if global_offset_z == 0bv32 then 1bv1 else 0bv1) != 0bv1;

const {:local_id_y} local_id_y$1: bv32;

const {:local_id_y} local_id_y$2: bv32;

const {:local_id_z} local_id_z$1: bv32;

const {:local_id_z} local_id_z$2: bv32;

const {:group_id_y} group_id_y$1: bv32;

const {:group_id_y} group_id_y$2: bv32;

const {:group_id_z} group_id_z$1: bv32;

const {:group_id_z} group_id_z$2: bv32;

procedure {:inline 1} {:safe_barrier} {:source_name "bugle_barrier"} {:barrier} $bugle_barrier_duplicated_0($0: bv1, $1: bv1);
  requires $0 == 1bv1;
  requires $1 == 1bv1;
  modifies $$binomial_options_kernel.call_a, $$binomial_options_kernel.call_b, $$call_value, $$call_buffer, _TRACKING;

procedure {:inline 1} {:safe_barrier} {:source_name "bugle_barrier"} {:barrier} $bugle_barrier_duplicated_1($0: bv1, $1: bv1);
  requires $0 == 1bv1;
  requires $1 == 1bv1;
  modifies $$binomial_options_kernel.call_a, $$binomial_options_kernel.call_b, $$call_value, $$call_buffer, _TRACKING;

procedure {:inline 1} {:safe_barrier} {:source_name "bugle_barrier"} {:barrier} $bugle_barrier_duplicated_2($0: bv1, $1: bv1);
  requires $0 == 1bv1;
  requires $1 == 1bv1;
  modifies $$binomial_options_kernel.call_a, $$binomial_options_kernel.call_b, $$call_value, $$call_buffer, _TRACKING;

procedure {:inline 1} {:safe_barrier} {:source_name "bugle_barrier"} {:barrier} $bugle_barrier_duplicated_3($0: bv1, $1: bv1);
  requires $0 == 1bv1;
  requires $1 == 1bv1;
  modifies $$binomial_options_kernel.call_a, $$binomial_options_kernel.call_b, $$call_value, $$call_buffer, _TRACKING;

const _WATCHED_VALUE_$$call_value: bv32;

procedure {:inline 1} _LOG_READ_$$call_value(_P: bool, _offset: bv32, _value: bv32);
  modifies _READ_HAS_OCCURRED_$$call_value;

implementation {:inline 1} _LOG_READ_$$call_value(_P: bool, _offset: bv32, _value: bv32)
{

  log_access_entry:
    _READ_HAS_OCCURRED_$$call_value := (if _P && _TRACKING && _WATCHED_OFFSET == _offset && _WATCHED_VALUE_$$call_value == _value then true else _READ_HAS_OCCURRED_$$call_value);
    return;
}

procedure _CHECK_READ_$$call_value(_P: bool, _offset: bv32, _value: bv32);
  requires !(_P && _WRITE_HAS_OCCURRED_$$call_value && _WATCHED_OFFSET == _offset && _WRITE_READ_BENIGN_FLAG_$$call_value);
  requires !(_P && _ATOMIC_HAS_OCCURRED_$$call_value && _WATCHED_OFFSET == _offset);

var _WRITE_READ_BENIGN_FLAG_$$call_value: bool;

procedure {:inline 1} _LOG_WRITE_$$call_value(_P: bool, _offset: bv32, _value: bv32, _value_old: bv32);
  modifies _WRITE_HAS_OCCURRED_$$call_value, _WRITE_READ_BENIGN_FLAG_$$call_value;

implementation {:inline 1} _LOG_WRITE_$$call_value(_P: bool, _offset: bv32, _value: bv32, _value_old: bv32)
{

  log_access_entry:
    _WRITE_HAS_OCCURRED_$$call_value := (if _P && _TRACKING && _WATCHED_OFFSET == _offset && _WATCHED_VALUE_$$call_value == _value then true else _WRITE_HAS_OCCURRED_$$call_value);
    _WRITE_READ_BENIGN_FLAG_$$call_value := (if _P && _TRACKING && _WATCHED_OFFSET == _offset && _WATCHED_VALUE_$$call_value == _value then _value != _value_old else _WRITE_READ_BENIGN_FLAG_$$call_value);
    return;
}

procedure _CHECK_WRITE_$$call_value(_P: bool, _offset: bv32, _value: bv32);
  requires !(_P && _WRITE_HAS_OCCURRED_$$call_value && _WATCHED_OFFSET == _offset && _WATCHED_VALUE_$$call_value != _value);
  requires !(_P && _READ_HAS_OCCURRED_$$call_value && _WATCHED_OFFSET == _offset && _WATCHED_VALUE_$$call_value != _value);
  requires !(_P && _ATOMIC_HAS_OCCURRED_$$call_value && _WATCHED_OFFSET == _offset);

procedure {:inline 1} _LOG_ATOMIC_$$call_value(_P: bool, _offset: bv32);
  modifies _ATOMIC_HAS_OCCURRED_$$call_value;

implementation {:inline 1} _LOG_ATOMIC_$$call_value(_P: bool, _offset: bv32)
{

  log_access_entry:
    _ATOMIC_HAS_OCCURRED_$$call_value := (if _P && _TRACKING && _WATCHED_OFFSET == _offset then true else _ATOMIC_HAS_OCCURRED_$$call_value);
    return;
}

procedure _CHECK_ATOMIC_$$call_value(_P: bool, _offset: bv32);
  requires !(_P && _WRITE_HAS_OCCURRED_$$call_value && _WATCHED_OFFSET == _offset);
  requires !(_P && _READ_HAS_OCCURRED_$$call_value && _WATCHED_OFFSET == _offset);

procedure {:inline 1} _UPDATE_WRITE_READ_BENIGN_FLAG_$$call_value(_P: bool, _offset: bv32);
  modifies _WRITE_READ_BENIGN_FLAG_$$call_value;

implementation {:inline 1} _UPDATE_WRITE_READ_BENIGN_FLAG_$$call_value(_P: bool, _offset: bv32)
{

  _UPDATE_BENIGN_FLAG:
    _WRITE_READ_BENIGN_FLAG_$$call_value := (if _P && _WRITE_HAS_OCCURRED_$$call_value && _WATCHED_OFFSET == _offset then false else _WRITE_READ_BENIGN_FLAG_$$call_value);
    return;
}

const _WATCHED_VALUE_$$call_buffer: bv32;

procedure {:inline 1} _LOG_READ_$$call_buffer(_P: bool, _offset: bv32, _value: bv32);
  modifies _READ_HAS_OCCURRED_$$call_buffer;

implementation {:inline 1} _LOG_READ_$$call_buffer(_P: bool, _offset: bv32, _value: bv32)
{

  log_access_entry:
    _READ_HAS_OCCURRED_$$call_buffer := (if _P && _TRACKING && _WATCHED_OFFSET == _offset && _WATCHED_VALUE_$$call_buffer == _value then true else _READ_HAS_OCCURRED_$$call_buffer);
    return;
}

procedure _CHECK_READ_$$call_buffer(_P: bool, _offset: bv32, _value: bv32);
  requires !(_P && _WRITE_HAS_OCCURRED_$$call_buffer && _WATCHED_OFFSET == _offset && _WRITE_READ_BENIGN_FLAG_$$call_buffer);
  requires !(_P && _ATOMIC_HAS_OCCURRED_$$call_buffer && _WATCHED_OFFSET == _offset);

var _WRITE_READ_BENIGN_FLAG_$$call_buffer: bool;

procedure {:inline 1} _LOG_WRITE_$$call_buffer(_P: bool, _offset: bv32, _value: bv32, _value_old: bv32);
  modifies _WRITE_HAS_OCCURRED_$$call_buffer, _WRITE_READ_BENIGN_FLAG_$$call_buffer;

implementation {:inline 1} _LOG_WRITE_$$call_buffer(_P: bool, _offset: bv32, _value: bv32, _value_old: bv32)
{

  log_access_entry:
    _WRITE_HAS_OCCURRED_$$call_buffer := (if _P && _TRACKING && _WATCHED_OFFSET == _offset && _WATCHED_VALUE_$$call_buffer == _value then true else _WRITE_HAS_OCCURRED_$$call_buffer);
    _WRITE_READ_BENIGN_FLAG_$$call_buffer := (if _P && _TRACKING && _WATCHED_OFFSET == _offset && _WATCHED_VALUE_$$call_buffer == _value then _value != _value_old else _WRITE_READ_BENIGN_FLAG_$$call_buffer);
    return;
}

procedure _CHECK_WRITE_$$call_buffer(_P: bool, _offset: bv32, _value: bv32);
  requires !(_P && _WRITE_HAS_OCCURRED_$$call_buffer && _WATCHED_OFFSET == _offset && _WATCHED_VALUE_$$call_buffer != _value);
  requires !(_P && _READ_HAS_OCCURRED_$$call_buffer && _WATCHED_OFFSET == _offset && _WATCHED_VALUE_$$call_buffer != _value);
  requires !(_P && _ATOMIC_HAS_OCCURRED_$$call_buffer && _WATCHED_OFFSET == _offset);

procedure {:inline 1} _LOG_ATOMIC_$$call_buffer(_P: bool, _offset: bv32);
  modifies _ATOMIC_HAS_OCCURRED_$$call_buffer;

implementation {:inline 1} _LOG_ATOMIC_$$call_buffer(_P: bool, _offset: bv32)
{

  log_access_entry:
    _ATOMIC_HAS_OCCURRED_$$call_buffer := (if _P && _TRACKING && _WATCHED_OFFSET == _offset then true else _ATOMIC_HAS_OCCURRED_$$call_buffer);
    return;
}

procedure _CHECK_ATOMIC_$$call_buffer(_P: bool, _offset: bv32);
  requires !(_P && _WRITE_HAS_OCCURRED_$$call_buffer && _WATCHED_OFFSET == _offset);
  requires !(_P && _READ_HAS_OCCURRED_$$call_buffer && _WATCHED_OFFSET == _offset);

procedure {:inline 1} _UPDATE_WRITE_READ_BENIGN_FLAG_$$call_buffer(_P: bool, _offset: bv32);
  modifies _WRITE_READ_BENIGN_FLAG_$$call_buffer;

implementation {:inline 1} _UPDATE_WRITE_READ_BENIGN_FLAG_$$call_buffer(_P: bool, _offset: bv32)
{

  _UPDATE_BENIGN_FLAG:
    _WRITE_READ_BENIGN_FLAG_$$call_buffer := (if _P && _WRITE_HAS_OCCURRED_$$call_buffer && _WATCHED_OFFSET == _offset then false else _WRITE_READ_BENIGN_FLAG_$$call_buffer);
    return;
}

const _WATCHED_VALUE_$$binomial_options_kernel.call_a: bv32;

procedure {:inline 1} _LOG_READ_$$binomial_options_kernel.call_a(_P: bool, _offset: bv32, _value: bv32);
  modifies _READ_HAS_OCCURRED_$$binomial_options_kernel.call_a;

implementation {:inline 1} _LOG_READ_$$binomial_options_kernel.call_a(_P: bool, _offset: bv32, _value: bv32)
{

  log_access_entry:
    _READ_HAS_OCCURRED_$$binomial_options_kernel.call_a := (if _P && group_id_x$1 == group_id_x$2 && group_id_y$1 == group_id_y$2 && group_id_z$1 == group_id_z$2 && _TRACKING && _WATCHED_OFFSET == _offset && _WATCHED_VALUE_$$binomial_options_kernel.call_a == _value then true else _READ_HAS_OCCURRED_$$binomial_options_kernel.call_a);
    return;
}

procedure _CHECK_READ_$$binomial_options_kernel.call_a(_P: bool, _offset: bv32, _value: bv32);
  requires !(_P && _WRITE_HAS_OCCURRED_$$binomial_options_kernel.call_a && _WATCHED_OFFSET == _offset && _WRITE_READ_BENIGN_FLAG_$$binomial_options_kernel.call_a && group_id_x$1 == group_id_x$2 && group_id_y$1 == group_id_y$2 && group_id_z$1 == group_id_z$2);
  requires !(_P && _ATOMIC_HAS_OCCURRED_$$binomial_options_kernel.call_a && _WATCHED_OFFSET == _offset && group_id_x$1 == group_id_x$2 && group_id_y$1 == group_id_y$2 && group_id_z$1 == group_id_z$2);

var _WRITE_READ_BENIGN_FLAG_$$binomial_options_kernel.call_a: bool;

procedure {:inline 1} _LOG_WRITE_$$binomial_options_kernel.call_a(_P: bool, _offset: bv32, _value: bv32, _value_old: bv32);
  modifies _WRITE_HAS_OCCURRED_$$binomial_options_kernel.call_a, _WRITE_READ_BENIGN_FLAG_$$binomial_options_kernel.call_a;

implementation {:inline 1} _LOG_WRITE_$$binomial_options_kernel.call_a(_P: bool, _offset: bv32, _value: bv32, _value_old: bv32)
{

  log_access_entry:
    _WRITE_HAS_OCCURRED_$$binomial_options_kernel.call_a := (if _P && group_id_x$1 == group_id_x$2 && group_id_y$1 == group_id_y$2 && group_id_z$1 == group_id_z$2 && _TRACKING && _WATCHED_OFFSET == _offset && _WATCHED_VALUE_$$binomial_options_kernel.call_a == _value then true else _WRITE_HAS_OCCURRED_$$binomial_options_kernel.call_a);
    _WRITE_READ_BENIGN_FLAG_$$binomial_options_kernel.call_a := (if _P && group_id_x$1 == group_id_x$2 && group_id_y$1 == group_id_y$2 && group_id_z$1 == group_id_z$2 && _TRACKING && _WATCHED_OFFSET == _offset && _WATCHED_VALUE_$$binomial_options_kernel.call_a == _value then _value != _value_old else _WRITE_READ_BENIGN_FLAG_$$binomial_options_kernel.call_a);
    return;
}

procedure _CHECK_WRITE_$$binomial_options_kernel.call_a(_P: bool, _offset: bv32, _value: bv32);
  requires !(_P && _WRITE_HAS_OCCURRED_$$binomial_options_kernel.call_a && _WATCHED_OFFSET == _offset && _WATCHED_VALUE_$$binomial_options_kernel.call_a != _value && group_id_x$1 == group_id_x$2 && group_id_y$1 == group_id_y$2 && group_id_z$1 == group_id_z$2);
  requires !(_P && _READ_HAS_OCCURRED_$$binomial_options_kernel.call_a && _WATCHED_OFFSET == _offset && _WATCHED_VALUE_$$binomial_options_kernel.call_a != _value && group_id_x$1 == group_id_x$2 && group_id_y$1 == group_id_y$2 && group_id_z$1 == group_id_z$2);
  requires !(_P && _ATOMIC_HAS_OCCURRED_$$binomial_options_kernel.call_a && _WATCHED_OFFSET == _offset && group_id_x$1 == group_id_x$2 && group_id_y$1 == group_id_y$2 && group_id_z$1 == group_id_z$2);

procedure {:inline 1} _LOG_ATOMIC_$$binomial_options_kernel.call_a(_P: bool, _offset: bv32);
  modifies _ATOMIC_HAS_OCCURRED_$$binomial_options_kernel.call_a;

implementation {:inline 1} _LOG_ATOMIC_$$binomial_options_kernel.call_a(_P: bool, _offset: bv32)
{

  log_access_entry:
    _ATOMIC_HAS_OCCURRED_$$binomial_options_kernel.call_a := (if _P && group_id_x$1 == group_id_x$2 && group_id_y$1 == group_id_y$2 && group_id_z$1 == group_id_z$2 && _TRACKING && _WATCHED_OFFSET == _offset then true else _ATOMIC_HAS_OCCURRED_$$binomial_options_kernel.call_a);
    return;
}

procedure _CHECK_ATOMIC_$$binomial_options_kernel.call_a(_P: bool, _offset: bv32);
  requires !(_P && _WRITE_HAS_OCCURRED_$$binomial_options_kernel.call_a && _WATCHED_OFFSET == _offset && group_id_x$1 == group_id_x$2 && group_id_y$1 == group_id_y$2 && group_id_z$1 == group_id_z$2);
  requires !(_P && _READ_HAS_OCCURRED_$$binomial_options_kernel.call_a && _WATCHED_OFFSET == _offset && group_id_x$1 == group_id_x$2 && group_id_y$1 == group_id_y$2 && group_id_z$1 == group_id_z$2);

procedure {:inline 1} _UPDATE_WRITE_READ_BENIGN_FLAG_$$binomial_options_kernel.call_a(_P: bool, _offset: bv32);
  modifies _WRITE_READ_BENIGN_FLAG_$$binomial_options_kernel.call_a;

implementation {:inline 1} _UPDATE_WRITE_READ_BENIGN_FLAG_$$binomial_options_kernel.call_a(_P: bool, _offset: bv32)
{

  _UPDATE_BENIGN_FLAG:
    _WRITE_READ_BENIGN_FLAG_$$binomial_options_kernel.call_a := (if _P && _WRITE_HAS_OCCURRED_$$binomial_options_kernel.call_a && _WATCHED_OFFSET == _offset then false else _WRITE_READ_BENIGN_FLAG_$$binomial_options_kernel.call_a);
    return;
}

const _WATCHED_VALUE_$$binomial_options_kernel.call_b: bv32;

procedure {:inline 1} _LOG_READ_$$binomial_options_kernel.call_b(_P: bool, _offset: bv32, _value: bv32);
  modifies _READ_HAS_OCCURRED_$$binomial_options_kernel.call_b;

implementation {:inline 1} _LOG_READ_$$binomial_options_kernel.call_b(_P: bool, _offset: bv32, _value: bv32)
{

  log_access_entry:
    _READ_HAS_OCCURRED_$$binomial_options_kernel.call_b := (if _P && group_id_x$1 == group_id_x$2 && group_id_y$1 == group_id_y$2 && group_id_z$1 == group_id_z$2 && _TRACKING && _WATCHED_OFFSET == _offset && _WATCHED_VALUE_$$binomial_options_kernel.call_b == _value then true else _READ_HAS_OCCURRED_$$binomial_options_kernel.call_b);
    return;
}

procedure _CHECK_READ_$$binomial_options_kernel.call_b(_P: bool, _offset: bv32, _value: bv32);
  requires !(_P && _WRITE_HAS_OCCURRED_$$binomial_options_kernel.call_b && _WATCHED_OFFSET == _offset && _WRITE_READ_BENIGN_FLAG_$$binomial_options_kernel.call_b && group_id_x$1 == group_id_x$2 && group_id_y$1 == group_id_y$2 && group_id_z$1 == group_id_z$2);
  requires !(_P && _ATOMIC_HAS_OCCURRED_$$binomial_options_kernel.call_b && _WATCHED_OFFSET == _offset && group_id_x$1 == group_id_x$2 && group_id_y$1 == group_id_y$2 && group_id_z$1 == group_id_z$2);

var _WRITE_READ_BENIGN_FLAG_$$binomial_options_kernel.call_b: bool;

procedure {:inline 1} _LOG_WRITE_$$binomial_options_kernel.call_b(_P: bool, _offset: bv32, _value: bv32, _value_old: bv32);
  modifies _WRITE_HAS_OCCURRED_$$binomial_options_kernel.call_b, _WRITE_READ_BENIGN_FLAG_$$binomial_options_kernel.call_b;

implementation {:inline 1} _LOG_WRITE_$$binomial_options_kernel.call_b(_P: bool, _offset: bv32, _value: bv32, _value_old: bv32)
{

  log_access_entry:
    _WRITE_HAS_OCCURRED_$$binomial_options_kernel.call_b := (if _P && group_id_x$1 == group_id_x$2 && group_id_y$1 == group_id_y$2 && group_id_z$1 == group_id_z$2 && _TRACKING && _WATCHED_OFFSET == _offset && _WATCHED_VALUE_$$binomial_options_kernel.call_b == _value then true else _WRITE_HAS_OCCURRED_$$binomial_options_kernel.call_b);
    _WRITE_READ_BENIGN_FLAG_$$binomial_options_kernel.call_b := (if _P && group_id_x$1 == group_id_x$2 && group_id_y$1 == group_id_y$2 && group_id_z$1 == group_id_z$2 && _TRACKING && _WATCHED_OFFSET == _offset && _WATCHED_VALUE_$$binomial_options_kernel.call_b == _value then _value != _value_old else _WRITE_READ_BENIGN_FLAG_$$binomial_options_kernel.call_b);
    return;
}

procedure _CHECK_WRITE_$$binomial_options_kernel.call_b(_P: bool, _offset: bv32, _value: bv32);
  requires !(_P && _WRITE_HAS_OCCURRED_$$binomial_options_kernel.call_b && _WATCHED_OFFSET == _offset && _WATCHED_VALUE_$$binomial_options_kernel.call_b != _value && group_id_x$1 == group_id_x$2 && group_id_y$1 == group_id_y$2 && group_id_z$1 == group_id_z$2);
  requires !(_P && _READ_HAS_OCCURRED_$$binomial_options_kernel.call_b && _WATCHED_OFFSET == _offset && _WATCHED_VALUE_$$binomial_options_kernel.call_b != _value && group_id_x$1 == group_id_x$2 && group_id_y$1 == group_id_y$2 && group_id_z$1 == group_id_z$2);
  requires !(_P && _ATOMIC_HAS_OCCURRED_$$binomial_options_kernel.call_b && _WATCHED_OFFSET == _offset && group_id_x$1 == group_id_x$2 && group_id_y$1 == group_id_y$2 && group_id_z$1 == group_id_z$2);

procedure {:inline 1} _LOG_ATOMIC_$$binomial_options_kernel.call_b(_P: bool, _offset: bv32);
  modifies _ATOMIC_HAS_OCCURRED_$$binomial_options_kernel.call_b;

implementation {:inline 1} _LOG_ATOMIC_$$binomial_options_kernel.call_b(_P: bool, _offset: bv32)
{

  log_access_entry:
    _ATOMIC_HAS_OCCURRED_$$binomial_options_kernel.call_b := (if _P && group_id_x$1 == group_id_x$2 && group_id_y$1 == group_id_y$2 && group_id_z$1 == group_id_z$2 && _TRACKING && _WATCHED_OFFSET == _offset then true else _ATOMIC_HAS_OCCURRED_$$binomial_options_kernel.call_b);
    return;
}

procedure _CHECK_ATOMIC_$$binomial_options_kernel.call_b(_P: bool, _offset: bv32);
  requires !(_P && _WRITE_HAS_OCCURRED_$$binomial_options_kernel.call_b && _WATCHED_OFFSET == _offset && group_id_x$1 == group_id_x$2 && group_id_y$1 == group_id_y$2 && group_id_z$1 == group_id_z$2);
  requires !(_P && _READ_HAS_OCCURRED_$$binomial_options_kernel.call_b && _WATCHED_OFFSET == _offset && group_id_x$1 == group_id_x$2 && group_id_y$1 == group_id_y$2 && group_id_z$1 == group_id_z$2);

procedure {:inline 1} _UPDATE_WRITE_READ_BENIGN_FLAG_$$binomial_options_kernel.call_b(_P: bool, _offset: bv32);
  modifies _WRITE_READ_BENIGN_FLAG_$$binomial_options_kernel.call_b;

implementation {:inline 1} _UPDATE_WRITE_READ_BENIGN_FLAG_$$binomial_options_kernel.call_b(_P: bool, _offset: bv32)
{

  _UPDATE_BENIGN_FLAG:
    _WRITE_READ_BENIGN_FLAG_$$binomial_options_kernel.call_b := (if _P && _WRITE_HAS_OCCURRED_$$binomial_options_kernel.call_b && _WATCHED_OFFSET == _offset then false else _WRITE_READ_BENIGN_FLAG_$$binomial_options_kernel.call_b);
    return;
}

var _TRACKING: bool;

implementation {:inline 1} $bugle_barrier_duplicated_0($0: bv1, $1: bv1)
{

  __BarrierImpl:
    goto anon11_Then, anon11_Else;

  anon11_Else:
    assume {:partition} true;
    goto anon0;

  anon0:
    assume $0 != 0bv1 ==> !_READ_HAS_OCCURRED_$$binomial_options_kernel.call_a;
    assume $0 != 0bv1 ==> !_WRITE_HAS_OCCURRED_$$binomial_options_kernel.call_a;
    assume $0 != 0bv1 ==> !_ATOMIC_HAS_OCCURRED_$$binomial_options_kernel.call_a;
    goto anon1;

  anon1:
    assume $0 != 0bv1 ==> !_READ_HAS_OCCURRED_$$binomial_options_kernel.call_b;
    assume $0 != 0bv1 ==> !_WRITE_HAS_OCCURRED_$$binomial_options_kernel.call_b;
    assume $0 != 0bv1 ==> !_ATOMIC_HAS_OCCURRED_$$binomial_options_kernel.call_b;
    goto anon2;

  anon2:
    goto anon12_Then, anon12_Else;

  anon12_Else:
    assume {:partition} !($0 != 0bv1 || $0 != 0bv1);
    goto anon5;

  anon5:
    assume group_id_x$1 == group_id_x$2 && group_id_y$1 == group_id_y$2 && group_id_z$1 == group_id_z$2 ==> $1 != 0bv1 ==> !_READ_HAS_OCCURRED_$$call_value;
    assume group_id_x$1 == group_id_x$2 && group_id_y$1 == group_id_y$2 && group_id_z$1 == group_id_z$2 ==> $1 != 0bv1 ==> !_WRITE_HAS_OCCURRED_$$call_value;
    assume group_id_x$1 == group_id_x$2 && group_id_y$1 == group_id_y$2 && group_id_z$1 == group_id_z$2 ==> $1 != 0bv1 ==> !_ATOMIC_HAS_OCCURRED_$$call_value;
    goto anon6;

  anon6:
    assume group_id_x$1 == group_id_x$2 && group_id_y$1 == group_id_y$2 && group_id_z$1 == group_id_z$2 ==> $1 != 0bv1 ==> !_READ_HAS_OCCURRED_$$call_buffer;
    assume group_id_x$1 == group_id_x$2 && group_id_y$1 == group_id_y$2 && group_id_z$1 == group_id_z$2 ==> $1 != 0bv1 ==> !_WRITE_HAS_OCCURRED_$$call_buffer;
    assume group_id_x$1 == group_id_x$2 && group_id_y$1 == group_id_y$2 && group_id_z$1 == group_id_z$2 ==> $1 != 0bv1 ==> !_ATOMIC_HAS_OCCURRED_$$call_buffer;
    goto anon7;

  anon7:
    goto anon13_Then, anon13_Else;

  anon13_Else:
    assume {:partition} !(group_id_x$1 == group_id_x$2 && group_id_y$1 == group_id_y$2 && group_id_z$1 == group_id_z$2 && ($1 != 0bv1 || $1 != 0bv1));
    goto anon10;

  anon10:
    havoc _TRACKING;
    return;

  anon13_Then:
    assume {:partition} group_id_x$1 == group_id_x$2 && group_id_y$1 == group_id_y$2 && group_id_z$1 == group_id_z$2 && ($1 != 0bv1 || $1 != 0bv1);
    havoc $$call_value;
    goto anon9;

  anon9:
    havoc $$call_buffer;
    goto anon10;

  anon12_Then:
    assume {:partition} $0 != 0bv1 || $0 != 0bv1;
    havoc $$binomial_options_kernel.call_a;
    goto anon4;

  anon4:
    havoc $$binomial_options_kernel.call_b;
    goto anon5;

  anon11_Then:
    assume {:partition} false;
    goto __Disabled;

  __Disabled:
    return;
}

implementation {:inline 1} $bugle_barrier_duplicated_1($0: bv1, $1: bv1)
{

  __BarrierImpl:
    goto anon11_Then, anon11_Else;

  anon11_Else:
    assume {:partition} true;
    goto anon0;

  anon0:
    assume $0 != 0bv1 ==> !_READ_HAS_OCCURRED_$$binomial_options_kernel.call_a;
    assume $0 != 0bv1 ==> !_WRITE_HAS_OCCURRED_$$binomial_options_kernel.call_a;
    assume $0 != 0bv1 ==> !_ATOMIC_HAS_OCCURRED_$$binomial_options_kernel.call_a;
    goto anon1;

  anon1:
    assume $0 != 0bv1 ==> !_READ_HAS_OCCURRED_$$binomial_options_kernel.call_b;
    assume $0 != 0bv1 ==> !_WRITE_HAS_OCCURRED_$$binomial_options_kernel.call_b;
    assume $0 != 0bv1 ==> !_ATOMIC_HAS_OCCURRED_$$binomial_options_kernel.call_b;
    goto anon2;

  anon2:
    goto anon12_Then, anon12_Else;

  anon12_Else:
    assume {:partition} !($0 != 0bv1 || $0 != 0bv1);
    goto anon5;

  anon5:
    assume group_id_x$1 == group_id_x$2 && group_id_y$1 == group_id_y$2 && group_id_z$1 == group_id_z$2 ==> $1 != 0bv1 ==> !_READ_HAS_OCCURRED_$$call_value;
    assume group_id_x$1 == group_id_x$2 && group_id_y$1 == group_id_y$2 && group_id_z$1 == group_id_z$2 ==> $1 != 0bv1 ==> !_WRITE_HAS_OCCURRED_$$call_value;
    assume group_id_x$1 == group_id_x$2 && group_id_y$1 == group_id_y$2 && group_id_z$1 == group_id_z$2 ==> $1 != 0bv1 ==> !_ATOMIC_HAS_OCCURRED_$$call_value;
    goto anon6;

  anon6:
    assume group_id_x$1 == group_id_x$2 && group_id_y$1 == group_id_y$2 && group_id_z$1 == group_id_z$2 ==> $1 != 0bv1 ==> !_READ_HAS_OCCURRED_$$call_buffer;
    assume group_id_x$1 == group_id_x$2 && group_id_y$1 == group_id_y$2 && group_id_z$1 == group_id_z$2 ==> $1 != 0bv1 ==> !_WRITE_HAS_OCCURRED_$$call_buffer;
    assume group_id_x$1 == group_id_x$2 && group_id_y$1 == group_id_y$2 && group_id_z$1 == group_id_z$2 ==> $1 != 0bv1 ==> !_ATOMIC_HAS_OCCURRED_$$call_buffer;
    goto anon7;

  anon7:
    goto anon13_Then, anon13_Else;

  anon13_Else:
    assume {:partition} !(group_id_x$1 == group_id_x$2 && group_id_y$1 == group_id_y$2 && group_id_z$1 == group_id_z$2 && ($1 != 0bv1 || $1 != 0bv1));
    goto anon10;

  anon10:
    havoc _TRACKING;
    return;

  anon13_Then:
    assume {:partition} group_id_x$1 == group_id_x$2 && group_id_y$1 == group_id_y$2 && group_id_z$1 == group_id_z$2 && ($1 != 0bv1 || $1 != 0bv1);
    havoc $$call_value;
    goto anon9;

  anon9:
    havoc $$call_buffer;
    goto anon10;

  anon12_Then:
    assume {:partition} $0 != 0bv1 || $0 != 0bv1;
    havoc $$binomial_options_kernel.call_a;
    goto anon4;

  anon4:
    havoc $$binomial_options_kernel.call_b;
    goto anon5;

  anon11_Then:
    assume {:partition} false;
    goto __Disabled;

  __Disabled:
    return;
}

implementation {:inline 1} $bugle_barrier_duplicated_2($0: bv1, $1: bv1)
{

  __BarrierImpl:
    goto anon11_Then, anon11_Else;

  anon11_Else:
    assume {:partition} true;
    goto anon0;

  anon0:
    assume $0 != 0bv1 ==> !_READ_HAS_OCCURRED_$$binomial_options_kernel.call_a;
    assume $0 != 0bv1 ==> !_WRITE_HAS_OCCURRED_$$binomial_options_kernel.call_a;
    assume $0 != 0bv1 ==> !_ATOMIC_HAS_OCCURRED_$$binomial_options_kernel.call_a;
    goto anon1;

  anon1:
    assume $0 != 0bv1 ==> !_READ_HAS_OCCURRED_$$binomial_options_kernel.call_b;
    assume $0 != 0bv1 ==> !_WRITE_HAS_OCCURRED_$$binomial_options_kernel.call_b;
    assume $0 != 0bv1 ==> !_ATOMIC_HAS_OCCURRED_$$binomial_options_kernel.call_b;
    goto anon2;

  anon2:
    goto anon12_Then, anon12_Else;

  anon12_Else:
    assume {:partition} !($0 != 0bv1 || $0 != 0bv1);
    goto anon5;

  anon5:
    assume group_id_x$1 == group_id_x$2 && group_id_y$1 == group_id_y$2 && group_id_z$1 == group_id_z$2 ==> $1 != 0bv1 ==> !_READ_HAS_OCCURRED_$$call_value;
    assume group_id_x$1 == group_id_x$2 && group_id_y$1 == group_id_y$2 && group_id_z$1 == group_id_z$2 ==> $1 != 0bv1 ==> !_WRITE_HAS_OCCURRED_$$call_value;
    assume group_id_x$1 == group_id_x$2 && group_id_y$1 == group_id_y$2 && group_id_z$1 == group_id_z$2 ==> $1 != 0bv1 ==> !_ATOMIC_HAS_OCCURRED_$$call_value;
    goto anon6;

  anon6:
    assume group_id_x$1 == group_id_x$2 && group_id_y$1 == group_id_y$2 && group_id_z$1 == group_id_z$2 ==> $1 != 0bv1 ==> !_READ_HAS_OCCURRED_$$call_buffer;
    assume group_id_x$1 == group_id_x$2 && group_id_y$1 == group_id_y$2 && group_id_z$1 == group_id_z$2 ==> $1 != 0bv1 ==> !_WRITE_HAS_OCCURRED_$$call_buffer;
    assume group_id_x$1 == group_id_x$2 && group_id_y$1 == group_id_y$2 && group_id_z$1 == group_id_z$2 ==> $1 != 0bv1 ==> !_ATOMIC_HAS_OCCURRED_$$call_buffer;
    goto anon7;

  anon7:
    goto anon13_Then, anon13_Else;

  anon13_Else:
    assume {:partition} !(group_id_x$1 == group_id_x$2 && group_id_y$1 == group_id_y$2 && group_id_z$1 == group_id_z$2 && ($1 != 0bv1 || $1 != 0bv1));
    goto anon10;

  anon10:
    havoc _TRACKING;
    return;

  anon13_Then:
    assume {:partition} group_id_x$1 == group_id_x$2 && group_id_y$1 == group_id_y$2 && group_id_z$1 == group_id_z$2 && ($1 != 0bv1 || $1 != 0bv1);
    havoc $$call_value;
    goto anon9;

  anon9:
    havoc $$call_buffer;
    goto anon10;

  anon12_Then:
    assume {:partition} $0 != 0bv1 || $0 != 0bv1;
    havoc $$binomial_options_kernel.call_a;
    goto anon4;

  anon4:
    havoc $$binomial_options_kernel.call_b;
    goto anon5;

  anon11_Then:
    assume {:partition} false;
    goto __Disabled;

  __Disabled:
    return;
}

implementation {:inline 1} $bugle_barrier_duplicated_3($0: bv1, $1: bv1)
{

  __BarrierImpl:
    goto anon11_Then, anon11_Else;

  anon11_Else:
    assume {:partition} true;
    goto anon0;

  anon0:
    assume $0 != 0bv1 ==> !_READ_HAS_OCCURRED_$$binomial_options_kernel.call_a;
    assume $0 != 0bv1 ==> !_WRITE_HAS_OCCURRED_$$binomial_options_kernel.call_a;
    assume $0 != 0bv1 ==> !_ATOMIC_HAS_OCCURRED_$$binomial_options_kernel.call_a;
    goto anon1;

  anon1:
    assume $0 != 0bv1 ==> !_READ_HAS_OCCURRED_$$binomial_options_kernel.call_b;
    assume $0 != 0bv1 ==> !_WRITE_HAS_OCCURRED_$$binomial_options_kernel.call_b;
    assume $0 != 0bv1 ==> !_ATOMIC_HAS_OCCURRED_$$binomial_options_kernel.call_b;
    goto anon2;

  anon2:
    goto anon12_Then, anon12_Else;

  anon12_Else:
    assume {:partition} !($0 != 0bv1 || $0 != 0bv1);
    goto anon5;

  anon5:
    assume group_id_x$1 == group_id_x$2 && group_id_y$1 == group_id_y$2 && group_id_z$1 == group_id_z$2 ==> $1 != 0bv1 ==> !_READ_HAS_OCCURRED_$$call_value;
    assume group_id_x$1 == group_id_x$2 && group_id_y$1 == group_id_y$2 && group_id_z$1 == group_id_z$2 ==> $1 != 0bv1 ==> !_WRITE_HAS_OCCURRED_$$call_value;
    assume group_id_x$1 == group_id_x$2 && group_id_y$1 == group_id_y$2 && group_id_z$1 == group_id_z$2 ==> $1 != 0bv1 ==> !_ATOMIC_HAS_OCCURRED_$$call_value;
    goto anon6;

  anon6:
    assume group_id_x$1 == group_id_x$2 && group_id_y$1 == group_id_y$2 && group_id_z$1 == group_id_z$2 ==> $1 != 0bv1 ==> !_READ_HAS_OCCURRED_$$call_buffer;
    assume group_id_x$1 == group_id_x$2 && group_id_y$1 == group_id_y$2 && group_id_z$1 == group_id_z$2 ==> $1 != 0bv1 ==> !_WRITE_HAS_OCCURRED_$$call_buffer;
    assume group_id_x$1 == group_id_x$2 && group_id_y$1 == group_id_y$2 && group_id_z$1 == group_id_z$2 ==> $1 != 0bv1 ==> !_ATOMIC_HAS_OCCURRED_$$call_buffer;
    goto anon7;

  anon7:
    goto anon13_Then, anon13_Else;

  anon13_Else:
    assume {:partition} !(group_id_x$1 == group_id_x$2 && group_id_y$1 == group_id_y$2 && group_id_z$1 == group_id_z$2 && ($1 != 0bv1 || $1 != 0bv1));
    goto anon10;

  anon10:
    havoc _TRACKING;
    return;

  anon13_Then:
    assume {:partition} group_id_x$1 == group_id_x$2 && group_id_y$1 == group_id_y$2 && group_id_z$1 == group_id_z$2 && ($1 != 0bv1 || $1 != 0bv1);
    havoc $$call_value;
    goto anon9;

  anon9:
    havoc $$call_buffer;
    goto anon10;

  anon12_Then:
    assume {:partition} $0 != 0bv1 || $0 != 0bv1;
    havoc $$binomial_options_kernel.call_a;
    goto anon4;

  anon4:
    havoc $$binomial_options_kernel.call_b;
    goto anon5;

  anon11_Then:
    assume {:partition} false;
    goto __Disabled;

  __Disabled:
    return;
}

