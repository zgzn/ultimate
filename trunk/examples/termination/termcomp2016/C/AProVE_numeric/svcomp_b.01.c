//#termcomp16-someonesaidyes
//#termcomp16-someonesaidyes
extern int __VERIFIER_nondet_int(void);

int test_fun(int x, int y)
{
    int c = 0;
    while (x > y) {
        x = x - 1;
        c = c + 1;
    }
    return c;
}

int main() {
    return test_fun(__VERIFIER_nondet_int(), __VERIFIER_nondet_int());
}
