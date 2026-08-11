package com.sandymandy.pleasurehorizons.networking.codec;

import com.mojang.datafixers.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;

public interface Function17<
        T1, T2, T3, T4, T5, T6, T7, T8, T9, T10,
        T11, T12, T13, T14, T15, T16, T17,
        R
        > {

    R apply(
            T1 t1, T2 t2, T3 t3, T4 t4, T5 t5, T6 t6, T7 t7, T8 t8, T9 t9,
            T10 t10, T11 t11, T12 t12, T13 t13, T14 t14, T15 t15, T16 t16, T17 t17
    );

    // curry 1
    default Function<T1, Function16<
            T2, T3, T4, T5, T6, T7, T8, T9, T10,
            T11, T12, T13, T14, T15, T16, T17, R
            >> curry() {
        return t1 ->
                (t2, t3, t4, t5, t6, t7, t8, t9, t10,
                 t11, t12, t13, t14, t15, t16, t17)
                        -> apply(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13, t14, t15, t16, t17);
    }

    // curry 2
    default BiFunction<T1, T2, Function15<
            T3, T4, T5, T6, T7, T8, T9, T10,
            T11, T12, T13, T14, T15, T16, T17, R
            >> curry2() {
        return (t1, t2) ->
                (t3, t4, t5, t6, t7, t8, t9, t10,
                 t11, t12, t13, t14, t15, t16, t17)
                        -> apply(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13, t14, t15, t16, t17);
    }

    // curry 3
    default Function3<T1, T2, T3, Function14<
            T4, T5, T6, T7, T8, T9, T10,
            T11, T12, T13, T14, T15, T16, T17, R
            >> curry3() {
        return (t1, t2, t3) ->
                (t4, t5, t6, t7, t8, t9, t10,
                 t11, t12, t13, t14, t15, t16, t17)
                        -> apply(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13, t14, t15, t16, t17);
    }

    // curry 4
    default Function4<T1, T2, T3, T4, Function13<
            T5, T6, T7, T8, T9, T10,
            T11, T12, T13, T14, T15, T16, T17, R
            >> curry4() {
        return (t1, t2, t3, t4) ->
                (t5, t6, t7, t8, t9, t10,
                 t11, t12, t13, t14, t15, t16, t17)
                        -> apply(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13, t14, t15, t16, t17);
    }

    // curry 5
    default Function5<T1, T2, T3, T4, T5, Function12<
            T6, T7, T8, T9, T10, T11,
            T12, T13, T14, T15, T16, T17, R
            >> curry5() {
        return (t1, t2, t3, t4, t5) ->
                (t6, t7, t8, t9, t10, t11,
                 t12, t13, t14, t15, t16, t17)
                        -> apply(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13, t14, t15, t16, t17);
    }

    // curry 6
    default Function6<T1, T2, T3, T4, T5, T6, Function11<
            T7, T8, T9, T10, T11,
            T12, T13, T14, T15, T16, T17, R
            >> curry6() {
        return (t1, t2, t3, t4, t5, t6) ->
                (t7, t8, t9, t10, t11,
                 t12, t13, t14, t15, t16, t17)
                        -> apply(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13, t14, t15, t16, t17);
    }

    // curry 7
    default Function7<T1, T2, T3, T4, T5, T6, T7, Function10<
            T8, T9, T10, T11, T12,
            T13, T14, T15, T16, T17, R
            >> curry7() {
        return (t1, t2, t3, t4, t5, t6, t7) ->
                (t8, t9, t10, t11, t12,
                 t13, t14, t15, t16, t17)
                        -> apply(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13, t14, t15, t16, t17);
    }

    // curry 8
    default Function8<T1, T2, T3, T4, T5, T6, T7, T8, Function9<
            T9, T10, T11, T12,
            T13, T14, T15, T16, T17, R
            >> curry8() {
        return (t1, t2, t3, t4, t5, t6, t7, t8) ->
                (t9, t10, t11, t12,
                 t13, t14, t15, t16, t17)
                        -> apply(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13, t14, t15, t16, t17);
    }

    // curry 9
    default Function9<T1, T2, T3, T4, T5, T6, T7, T8, T9, Function8<
            T10, T11, T12, T13,
            T14, T15, T16, T17, R
            >> curry9() {
        return (t1, t2, t3, t4, t5, t6, t7, t8, t9) ->
                (t10, t11, t12, t13,
                 t14, t15, t16, t17)
                        -> apply(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13, t14, t15, t16, t17);
    }

    // curry 10
    default Function10<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, Function7<
            T11, T12, T13,
            T14, T15, T16, T17, R
            >> curry10() {
        return (t1, t2, t3, t4, t5, t6, t7, t8, t9, t10) ->
                (t11, t12, t13,
                 t14, t15, t16, t17)
                        -> apply(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13, t14, t15, t16, t17);
    }

    // curry 11
    default Function11<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, Function6<
            T12, T13,
            T14, T15, T16, T17, R
            >> curry11() {
        return (t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11) ->
                (t12, t13,
                 t14, t15, t16, t17)
                        -> apply(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13, t14, t15, t16, t17);
    }

    // curry 12
    default Function12<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, Function5<
            T13,
            T14, T15, T16, T17, R
            >> curry12() {
        return (t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12) ->
                (t13,
                 t14, t15, t16, t17)
                        -> apply(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13, t14, t15, t16, t17);
    }

    // curry 13
    default Function13<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, Function4<
            T14, T15, T16, T17, R
            >> curry13() {
        return (t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13) ->
                (t14, t15, t16, t17)
                        -> apply(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13, t14, t15, t16, t17);
    }

    // curry 14
    default Function14<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14,
            Function3<T15, T16, T17, R>
            > curry14() {
        return (t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13, t14) ->
                (t15, t16, t17)
                        -> apply(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13, t14, t15, t16, t17);
    }

    // curry 15
    default Function15<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15,
            BiFunction<T16, T17, R>
            > curry15() {
        return (t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13, t14, t15) ->
                (t16, t17)
                        -> apply(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13, t14, t15, t16, t17);
    }

    // curry 16
    default Function16<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16,
            Function<T17, R>
            > curry16() {
        return (t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13, t14, t15, t16) ->
                t17 -> apply(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13, t14, t15, t16, t17);
    }
}
