# Perf probe: one instrumented Java method call per run. The wrapped Pair's
# getFirst() goes through the DCR weaver's advice. Grinder refuses to
# instrument bootstrap classes, so the target is one of its own.
from net.grinder.script.Grinder import grinder
from net.grinder.script import Test
from net.grinder.util import Pair

pair = Test(1, "Pair.getFirst").wrap(Pair.of("first", "second"))

class TestRunner:
    def __call__(self):
        pair.getFirst()
