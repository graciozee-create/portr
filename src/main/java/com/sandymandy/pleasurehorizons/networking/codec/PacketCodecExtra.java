package com.sandymandy.pleasurehorizons.networking.codec;

import com.mojang.datafixers.util.*;
import net.minecraft.network.codec.PacketCodec;

import java.util.function.Function;

public interface PacketCodecExtra {
    static <B, C, T1, T2, T3, T4, T5, T6, T7, T8, T9> PacketCodec<B, C> tuple(
            PacketCodec<? super B, T1> c1, Function<C, T1> f1,
            PacketCodec<? super B, T2> c2, Function<C, T2> f2,
            PacketCodec<? super B, T3> c3, Function<C, T3> f3,
            PacketCodec<? super B, T4> c4, Function<C, T4> f4,
            PacketCodec<? super B, T5> c5, Function<C, T5> f5,
            PacketCodec<? super B, T6> c6, Function<C, T6> f6,
            PacketCodec<? super B, T7> c7, Function<C, T7> f7,
            PacketCodec<? super B, T8> c8, Function<C, T8> f8,
            PacketCodec<? super B, T9> c9, Function<C, T9> f9,
            Function9<T1, T2, T3, T4, T5, T6, T7, T8, T9, C> to
    ) {
        return new PacketCodec<>() {
            @Override
            public C decode(B buf) {
                return to.apply(
                        c1.decode(buf), c2.decode(buf), c3.decode(buf),
                        c4.decode(buf), c5.decode(buf), c6.decode(buf),
                        c7.decode(buf), c8.decode(buf), c9.decode(buf)
                );
            }

            @Override
            public void encode(B buf, C value) {
                c1.encode(buf, f1.apply(value));
                c2.encode(buf, f2.apply(value));
                c3.encode(buf, f3.apply(value));
                c4.encode(buf, f4.apply(value));
                c5.encode(buf, f5.apply(value));
                c6.encode(buf, f6.apply(value));
                c7.encode(buf, f7.apply(value));
                c8.encode(buf, f8.apply(value));
                c9.encode(buf, f9.apply(value));
            }
        };
    }

    static <B, C, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10> PacketCodec<B, C> tuple(
            PacketCodec<? super B, T1> c1, Function<C, T1> f1,
            PacketCodec<? super B, T2> c2, Function<C, T2> f2,
            PacketCodec<? super B, T3> c3, Function<C, T3> f3,
            PacketCodec<? super B, T4> c4, Function<C, T4> f4,
            PacketCodec<? super B, T5> c5, Function<C, T5> f5,
            PacketCodec<? super B, T6> c6, Function<C, T6> f6,
            PacketCodec<? super B, T7> c7, Function<C, T7> f7,
            PacketCodec<? super B, T8> c8, Function<C, T8> f8,
            PacketCodec<? super B, T9> c9, Function<C, T9> f9,
            PacketCodec<? super B, T10> c10, Function<C, T10> f10,
            Function10<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, C> to
    ) {
        return new PacketCodec<>() {
            @Override
            public C decode(B buf) {
                return to.apply(
                        c1.decode(buf), c2.decode(buf), c3.decode(buf),
                        c4.decode(buf), c5.decode(buf), c6.decode(buf),
                        c7.decode(buf), c8.decode(buf), c9.decode(buf),
                        c10.decode(buf)
                );
            }

            @Override
            public void encode(B buf, C value) {
                c1.encode(buf, f1.apply(value));
                c2.encode(buf, f2.apply(value));
                c3.encode(buf, f3.apply(value));
                c4.encode(buf, f4.apply(value));
                c5.encode(buf, f5.apply(value));
                c6.encode(buf, f6.apply(value));
                c7.encode(buf, f7.apply(value));
                c8.encode(buf, f8.apply(value));
                c9.encode(buf, f9.apply(value));
                c10.encode(buf, f10.apply(value));
            }
        };
    }

    static <B, C, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11> PacketCodec<B, C> tuple(
            PacketCodec<? super B, T1> c1, Function<C, T1> f1,
            PacketCodec<? super B, T2> c2, Function<C, T2> f2,
            PacketCodec<? super B, T3> c3, Function<C, T3> f3,
            PacketCodec<? super B, T4> c4, Function<C, T4> f4,
            PacketCodec<? super B, T5> c5, Function<C, T5> f5,
            PacketCodec<? super B, T6> c6, Function<C, T6> f6,
            PacketCodec<? super B, T7> c7, Function<C, T7> f7,
            PacketCodec<? super B, T8> c8, Function<C, T8> f8,
            PacketCodec<? super B, T9> c9, Function<C, T9> f9,
            PacketCodec<? super B, T10> c10, Function<C, T10> f10,
            PacketCodec<? super B, T11> c11, Function<C, T11> f11,
            Function11<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, C> to
    ) {
        return new PacketCodec<>() {
            @Override
            public C decode(B buf) {
                return to.apply(
                        c1.decode(buf), c2.decode(buf), c3.decode(buf),
                        c4.decode(buf), c5.decode(buf), c6.decode(buf),
                        c7.decode(buf), c8.decode(buf), c9.decode(buf),
                        c10.decode(buf), c11.decode(buf)
                );
            }

            @Override
            public void encode(B buf, C value) {
                c1.encode(buf, f1.apply(value));
                c2.encode(buf, f2.apply(value));
                c3.encode(buf, f3.apply(value));
                c4.encode(buf, f4.apply(value));
                c5.encode(buf, f5.apply(value));
                c6.encode(buf, f6.apply(value));
                c7.encode(buf, f7.apply(value));
                c8.encode(buf, f8.apply(value));
                c9.encode(buf, f9.apply(value));
                c10.encode(buf, f10.apply(value));
                c11.encode(buf, f11.apply(value));
            }
        };
    }

    static <B, C, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12> PacketCodec<B, C> tuple(
            PacketCodec<? super B, T1> c1, Function<C, T1> f1,
            PacketCodec<? super B, T2> c2, Function<C, T2> f2,
            PacketCodec<? super B, T3> c3, Function<C, T3> f3,
            PacketCodec<? super B, T4> c4, Function<C, T4> f4,
            PacketCodec<? super B, T5> c5, Function<C, T5> f5,
            PacketCodec<? super B, T6> c6, Function<C, T6> f6,
            PacketCodec<? super B, T7> c7, Function<C, T7> f7,
            PacketCodec<? super B, T8> c8, Function<C, T8> f8,
            PacketCodec<? super B, T9> c9, Function<C, T9> f9,
            PacketCodec<? super B, T10> c10, Function<C, T10> f10,
            PacketCodec<? super B, T11> c11, Function<C, T11> f11,
            PacketCodec<? super B, T12> c12, Function<C, T12> f12,
            Function12<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, C> to
    ) {
        return new PacketCodec<>() {
            @Override
            public C decode(B buf) {
                return to.apply(
                        c1.decode(buf), c2.decode(buf), c3.decode(buf),
                        c4.decode(buf), c5.decode(buf), c6.decode(buf),
                        c7.decode(buf), c8.decode(buf), c9.decode(buf),
                        c10.decode(buf), c11.decode(buf), c12.decode(buf)
                );
            }

            @Override
            public void encode(B buf, C value) {
                c1.encode(buf, f1.apply(value));
                c2.encode(buf, f2.apply(value));
                c3.encode(buf, f3.apply(value));
                c4.encode(buf, f4.apply(value));
                c5.encode(buf, f5.apply(value));
                c6.encode(buf, f6.apply(value));
                c7.encode(buf, f7.apply(value));
                c8.encode(buf, f8.apply(value));
                c9.encode(buf, f9.apply(value));
                c10.encode(buf, f10.apply(value));
                c11.encode(buf, f11.apply(value));
                c12.encode(buf, f12.apply(value));
            }
        };
    }

    static <B, C, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13> PacketCodec<B, C> tuple(
            PacketCodec<? super B, T1> c1, Function<C, T1> f1,
            PacketCodec<? super B, T2> c2, Function<C, T2> f2,
            PacketCodec<? super B, T3> c3, Function<C, T3> f3,
            PacketCodec<? super B, T4> c4, Function<C, T4> f4,
            PacketCodec<? super B, T5> c5, Function<C, T5> f5,
            PacketCodec<? super B, T6> c6, Function<C, T6> f6,
            PacketCodec<? super B, T7> c7, Function<C, T7> f7,
            PacketCodec<? super B, T8> c8, Function<C, T8> f8,
            PacketCodec<? super B, T9> c9, Function<C, T9> f9,
            PacketCodec<? super B, T10> c10, Function<C, T10> f10,
            PacketCodec<? super B, T11> c11, Function<C, T11> f11,
            PacketCodec<? super B, T12> c12, Function<C, T12> f12,
            PacketCodec<? super B, T13> c13, Function<C, T13> f13,
            Function13<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, C> to
    ) {
        return new PacketCodec<>() {
            @Override
            public C decode(B buf) {
                return to.apply(
                        c1.decode(buf), c2.decode(buf), c3.decode(buf),
                        c4.decode(buf), c5.decode(buf), c6.decode(buf),
                        c7.decode(buf), c8.decode(buf), c9.decode(buf),
                        c10.decode(buf), c11.decode(buf), c12.decode(buf),
                        c13.decode(buf)
                );
            }

            @Override
            public void encode(B buf, C value) {
                c1.encode(buf, f1.apply(value));
                c2.encode(buf, f2.apply(value));
                c3.encode(buf, f3.apply(value));
                c4.encode(buf, f4.apply(value));
                c5.encode(buf, f5.apply(value));
                c6.encode(buf, f6.apply(value));
                c7.encode(buf, f7.apply(value));
                c8.encode(buf, f8.apply(value));
                c9.encode(buf, f9.apply(value));
                c10.encode(buf, f10.apply(value));
                c11.encode(buf, f11.apply(value));
                c12.encode(buf, f12.apply(value));
                c13.encode(buf, f13.apply(value));
            }
        };
    }

    static <B, C, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14> PacketCodec<B, C> tuple(
            PacketCodec<? super B, T1> c1, Function<C, T1> f1,
            PacketCodec<? super B, T2> c2, Function<C, T2> f2,
            PacketCodec<? super B, T3> c3, Function<C, T3> f3,
            PacketCodec<? super B, T4> c4, Function<C, T4> f4,
            PacketCodec<? super B, T5> c5, Function<C, T5> f5,
            PacketCodec<? super B, T6> c6, Function<C, T6> f6,
            PacketCodec<? super B, T7> c7, Function<C, T7> f7,
            PacketCodec<? super B, T8> c8, Function<C, T8> f8,
            PacketCodec<? super B, T9> c9, Function<C, T9> f9,
            PacketCodec<? super B, T10> c10, Function<C, T10> f10,
            PacketCodec<? super B, T11> c11, Function<C, T11> f11,
            PacketCodec<? super B, T12> c12, Function<C, T12> f12,
            PacketCodec<? super B, T13> c13, Function<C, T13> f13,
            PacketCodec<? super B, T14> c14, Function<C, T14> f14,
            Function14<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, C> to
    ) {
        return new PacketCodec<>() {
            @Override
            public C decode(B buf) {
                return to.apply(
                        c1.decode(buf), c2.decode(buf), c3.decode(buf),
                        c4.decode(buf), c5.decode(buf), c6.decode(buf),
                        c7.decode(buf), c8.decode(buf), c9.decode(buf),
                        c10.decode(buf), c11.decode(buf), c12.decode(buf),
                        c13.decode(buf), c14.decode(buf)
                );
            }

            @Override
            public void encode(B buf, C value) {
                c1.encode(buf, f1.apply(value));
                c2.encode(buf, f2.apply(value));
                c3.encode(buf, f3.apply(value));
                c4.encode(buf, f4.apply(value));
                c5.encode(buf, f5.apply(value));
                c6.encode(buf, f6.apply(value));
                c7.encode(buf, f7.apply(value));
                c8.encode(buf, f8.apply(value));
                c9.encode(buf, f9.apply(value));
                c10.encode(buf, f10.apply(value));
                c11.encode(buf, f11.apply(value));
                c12.encode(buf, f12.apply(value));
                c13.encode(buf, f13.apply(value));
                c14.encode(buf, f14.apply(value));
            }
        };
    }

    static <B, C, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15> PacketCodec<B, C> tuple(
            PacketCodec<? super B, T1> c1, Function<C, T1> f1,
            PacketCodec<? super B, T2> c2, Function<C, T2> f2,
            PacketCodec<? super B, T3> c3, Function<C, T3> f3,
            PacketCodec<? super B, T4> c4, Function<C, T4> f4,
            PacketCodec<? super B, T5> c5, Function<C, T5> f5,
            PacketCodec<? super B, T6> c6, Function<C, T6> f6,
            PacketCodec<? super B, T7> c7, Function<C, T7> f7,
            PacketCodec<? super B, T8> c8, Function<C, T8> f8,
            PacketCodec<? super B, T9> c9, Function<C, T9> f9,
            PacketCodec<? super B, T10> c10, Function<C, T10> f10,
            PacketCodec<? super B, T11> c11, Function<C, T11> f11,
            PacketCodec<? super B, T12> c12, Function<C, T12> f12,
            PacketCodec<? super B, T13> c13, Function<C, T13> f13,
            PacketCodec<? super B, T14> c14, Function<C, T14> f14,
            PacketCodec<? super B, T15> c15, Function<C, T15> f15,
            Function15<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, C> to
    ) {
        return new PacketCodec<>() {
            @Override
            public C decode(B buf) {
                return to.apply(
                        c1.decode(buf), c2.decode(buf), c3.decode(buf),
                        c4.decode(buf), c5.decode(buf), c6.decode(buf),
                        c7.decode(buf), c8.decode(buf), c9.decode(buf),
                        c10.decode(buf), c11.decode(buf), c12.decode(buf),
                        c13.decode(buf), c14.decode(buf), c15.decode(buf)
                );
            }

            @Override
            public void encode(B buf, C value) {
                c1.encode(buf, f1.apply(value));
                c2.encode(buf, f2.apply(value));
                c3.encode(buf, f3.apply(value));
                c4.encode(buf, f4.apply(value));
                c5.encode(buf, f5.apply(value));
                c6.encode(buf, f6.apply(value));
                c7.encode(buf, f7.apply(value));
                c8.encode(buf, f8.apply(value));
                c9.encode(buf, f9.apply(value));
                c10.encode(buf, f10.apply(value));
                c11.encode(buf, f11.apply(value));
                c12.encode(buf, f12.apply(value));
                c13.encode(buf, f13.apply(value));
                c14.encode(buf, f14.apply(value));
                c15.encode(buf, f15.apply(value));
            }
        };
    }

    static <B, C, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16> PacketCodec<B, C> tuple(
            PacketCodec<? super B, T1> c1, Function<C, T1> f1,
            PacketCodec<? super B, T2> c2, Function<C, T2> f2,
            PacketCodec<? super B, T3> c3, Function<C, T3> f3,
            PacketCodec<? super B, T4> c4, Function<C, T4> f4,
            PacketCodec<? super B, T5> c5, Function<C, T5> f5,
            PacketCodec<? super B, T6> c6, Function<C, T6> f6,
            PacketCodec<? super B, T7> c7, Function<C, T7> f7,
            PacketCodec<? super B, T8> c8, Function<C, T8> f8,
            PacketCodec<? super B, T9> c9, Function<C, T9> f9,
            PacketCodec<? super B, T10> c10, Function<C, T10> f10,
            PacketCodec<? super B, T11> c11, Function<C, T11> f11,
            PacketCodec<? super B, T12> c12, Function<C, T12> f12,
            PacketCodec<? super B, T13> c13, Function<C, T13> f13,
            PacketCodec<? super B, T14> c14, Function<C, T14> f14,
            PacketCodec<? super B, T15> c15, Function<C, T15> f15,
            PacketCodec<? super B, T16> c16, Function<C, T16> f16,
            Function16<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, C> to
    ) {
        return new PacketCodec<>() {
            @Override
            public C decode(B buf) {
                return to.apply(
                        c1.decode(buf), c2.decode(buf), c3.decode(buf),
                        c4.decode(buf), c5.decode(buf), c6.decode(buf),
                        c7.decode(buf), c8.decode(buf), c9.decode(buf),
                        c10.decode(buf), c11.decode(buf), c12.decode(buf),
                        c13.decode(buf), c14.decode(buf), c15.decode(buf),
                        c16.decode(buf)
                );
            }

            @Override
            public void encode(B buf, C value) {
                c1.encode(buf, f1.apply(value));
                c2.encode(buf, f2.apply(value));
                c3.encode(buf, f3.apply(value));
                c4.encode(buf, f4.apply(value));
                c5.encode(buf, f5.apply(value));
                c6.encode(buf, f6.apply(value));
                c7.encode(buf, f7.apply(value));
                c8.encode(buf, f8.apply(value));
                c9.encode(buf, f9.apply(value));
                c10.encode(buf, f10.apply(value));
                c11.encode(buf, f11.apply(value));
                c12.encode(buf, f12.apply(value));
                c13.encode(buf, f13.apply(value));
                c14.encode(buf, f14.apply(value));
                c15.encode(buf, f15.apply(value));
                c16.encode(buf, f16.apply(value));
            }
        };
    }

    static <B, C, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17> PacketCodec<B, C> tuple(
            PacketCodec<? super B, T1> c1, Function<C, T1> f1,
            PacketCodec<? super B, T2> c2, Function<C, T2> f2,
            PacketCodec<? super B, T3> c3, Function<C, T3> f3,
            PacketCodec<? super B, T4> c4, Function<C, T4> f4,
            PacketCodec<? super B, T5> c5, Function<C, T5> f5,
            PacketCodec<? super B, T6> c6, Function<C, T6> f6,
            PacketCodec<? super B, T7> c7, Function<C, T7> f7,
            PacketCodec<? super B, T8> c8, Function<C, T8> f8,
            PacketCodec<? super B, T9> c9, Function<C, T9> f9,
            PacketCodec<? super B, T10> c10, Function<C, T10> f10,
            PacketCodec<? super B, T11> c11, Function<C, T11> f11,
            PacketCodec<? super B, T12> c12, Function<C, T12> f12,
            PacketCodec<? super B, T13> c13, Function<C, T13> f13,
            PacketCodec<? super B, T14> c14, Function<C, T14> f14,
            PacketCodec<? super B, T15> c15, Function<C, T15> f15,
            PacketCodec<? super B, T16> c16, Function<C, T16> f16,
            PacketCodec<? super B, T17> c17, Function<C, T17> f17,
            Function17<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, C> to
    ) {
        return new PacketCodec<>() {
            @Override
            public C decode(B buf) {
                return to.apply(
                        c1.decode(buf), c2.decode(buf), c3.decode(buf),
                        c4.decode(buf), c5.decode(buf), c6.decode(buf),
                        c7.decode(buf), c8.decode(buf), c9.decode(buf),
                        c10.decode(buf), c11.decode(buf), c12.decode(buf),
                        c13.decode(buf), c14.decode(buf), c15.decode(buf),
                        c16.decode(buf), c17.decode(buf)
                );
            }

            @Override
            public void encode(B buf, C value) {
                c1.encode(buf, f1.apply(value));
                c2.encode(buf, f2.apply(value));
                c3.encode(buf, f3.apply(value));
                c4.encode(buf, f4.apply(value));
                c5.encode(buf, f5.apply(value));
                c6.encode(buf, f6.apply(value));
                c7.encode(buf, f7.apply(value));
                c8.encode(buf, f8.apply(value));
                c9.encode(buf, f9.apply(value));
                c10.encode(buf, f10.apply(value));
                c11.encode(buf, f11.apply(value));
                c12.encode(buf, f12.apply(value));
                c13.encode(buf, f13.apply(value));
                c14.encode(buf, f14.apply(value));
                c15.encode(buf, f15.apply(value));
                c16.encode(buf, f16.apply(value));
                c17.encode(buf, f17.apply(value));
            }
        };
    }

}
