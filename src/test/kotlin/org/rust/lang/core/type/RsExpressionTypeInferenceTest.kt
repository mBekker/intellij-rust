package org.rust.lang.core.type

import org.junit.ComparisonFailure

class RsExpressionTypeInferenceTest : RsTypificationTestBase() {
    fun testFunctionCall() = testExpr("""
        struct S;

        fn new() -> S { S }

        fn main() {
            let x = new();
            x;
          //^ S
        }
    """)

    fun testUnitFunctionCall() = testExpr("""
        fn foo() {}
        fn main() {
            let x = foo();
            x;
          //^ ()
        }
    """)

    fun testStaticMethodCall() = testExpr("""
        struct S;
        struct T;
        impl S { fn new() -> T { T } }

        fn main() {
            let x = S::new();
            x;
          //^ T
        }
    """)

    fun testBlockExpr() = testExpr("""
        struct S;

        fn foo() -> S {}
        fn main() {
            let x = {
                foo()
            };

            x
          //^ S
        }
    """)

    fun testUnitBlockExpr() = testExpr("""
        struct S;

        fn foo() -> S {}
        fn main() {
            let x = {
                foo();
            };

            x
          //^ ()
        }
    """)

    fun testEmptyBlockExpr() = testExpr("""
        fn main() {
            let x = {};
            x
          //^ ()
        }
    """)

    fun testTypeParameters() = testExpr("""
        fn foo<FOO>(foo: FOO) {
            let bar = foo;
            bar
          //^ FOO
        }
    """)

    fun testUnitIf() = testExpr("""
        fn main() {
            let x = if true { 92 };
            x
          //^ ()
        }
    """)

    fun testIf() = testExpr("""
        fn main() {
            let x = if true { 92 } else { 62 };
            x
          //^ i32
        }
    """)

    fun testLoop() = testExpr("""
        fn main() {
            let x = loop { break; };
            x
          //^ ()
        }
    """)

    fun testWhile() = testExpr("""
        fn main() {
            let x = while false { 92 };
            x
          //^ ()
        }
    """)

    fun testFor() = testExpr("""
        fn main() {
            let x = for _ in 62..92 {};
            x
          //^ ()
        }
    """)

    fun testParenthesis() = testExpr("""
        fn main() {
            (false);
          //^ bool
        }
    """)

    fun testDefaultFloat() = testExpr("""
        fn main() {
            let a = 1.0;
                    //^ f64
        }
    """)

    fun testF32() = testExpr("""
        fn main() {
            let a = 1.0f32;
                    //^ f32
        }
    """)

    fun testF64() = testExpr("""
        fn main() {
            let a = 1.0f64;
                    //^ f64
        }
    """)

    fun testDefaultInteger() = testExpr("""
        fn main() {
            let a = 42;
                   //^ i32
        }
    """)

    fun testI8() = testExpr("""
        fn main() {
            let a = 42i8;
                   //^ i8
        }
    """)

    fun testI16() = testExpr("""
        fn main() {
            let a = 42i16;
                   //^ i16
        }
    """)

    fun testI32() = testExpr("""
        fn main() {
            let a = 42i32;
                   //^ i32
        }
    """)

    fun testI64() = testExpr("""
        fn main() {
            let a = 42i64;
                   //^ i64
        }
    """)

    fun testI128() = testExpr("""
        fn main() {
            let a = 42i128;
                   //^ i128
        }
    """)

    fun testISize() = testExpr("""
        fn main() {
            let a = 42isize;
                   //^ isize
        }
    """)

    fun testU8() = testExpr("""
        fn main() {
            let a = 42u8;
                   //^ u8
        }
    """)

    fun testU16() = testExpr("""
        fn main() {
            let a = 42u16;
                   //^ u16
        }
    """)

    fun testU32() = testExpr("""
        fn main() {
            let a = 42u32;
                   //^ u32
        }
    """)

    fun testU64() = testExpr("""
        fn main() {
            let a = 42u64;
                   //^ u64
        }
    """)

    fun testU128() = testExpr("""
        fn main() {
            let a = 42u128;
                   //^ u128
        }
    """)

    fun testUSize() = testExpr("""
        fn main() {
            let a = 42usize;
                   //^ usize
        }
    """)

    fun testBoolTrue() = testExpr("""
        fn main() {
            let a = true;
                     //^ bool
        }
    """)

    fun testBoolFalse() = testExpr("""
        fn main() {
            let a = false;
                      //^ bool
        }
    """)

    fun testChar() = testExpr("""
        fn main() {
            let a = 'A';
                   //^ char
        }
    """)

    fun testStrRef() = expect<ComparisonFailure> {
        testExpr("""
        fn main() {
            let a = "Hello";
                       //^ & str
        }
    """)
    }

    fun testEnumVariantA() = testExpr("""
        enum E { A(i32), B { val: bool }, C }
        fn main() {
            (E::A(92))
          //^ E
        }
    """)

    fun testEnumVariantB() = testExpr("""
        enum E { A(i32), B { val: bool }, C }
        fn main() {
            (E::B { val: 92 })
          //^ E
        }
    """)

    fun testEnumVariantC() = testExpr("""
        enum E { A(i32), B { val: bool }, C }
        fn main() {
            (E::C)
          //^ E
        }
    """)

    fun testMatchExpr() = testExpr("""
        struct S;

        enum E { A(S), B }

        fn foo(e: &E) {
            let s = match *e {
                E::A(ref x) => x,
                E::B => panic!(),
            };
            s
        } //^ S
    """)

    fun testNoStackOverflow1() = testExpr("""
        pub struct P<T: ?Sized> { ptr: Box<T> }

        #[allow(non_snake_case)]
        pub fn P<T: 'static>(value: T) -> P<T> {
            P { ptr: Box::new(value) }
        }

        fn main() {
            let x = P(92);
            x
          //^ P
        }
    """)

    fun testNoStackOverflow2() = testExpr("""
        fn foo(S: S){
            let x = S;
            x.foo()
              //^ <unknown>
        }
    """)

    fun testBinOperatorsBool() {
        val cases = listOf(
            Pair("1 == 2", "bool"),
            Pair("1 != 2", "bool"),
            Pair("1 <= 2", "bool"),
            Pair("1 >= 2", "bool"),
            Pair("1 < 2", "bool"),
            Pair("1 > 2", "bool"),
            Pair("true && false", "bool"),
            Pair("true || false", "bool")
        )

        for ((i, case) in cases.withIndex()) {
            testExpr("""
                fn main() {
                    let x = ${case.first};
                    x
                  //^ ${case.second}
                }
                """
                ,
                "Case number: $i")
        }
    }
}

