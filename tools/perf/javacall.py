# Perf probe: one instrumented Java method call per run. The wrapped
# StringBuilder's append() goes through the DCR weaver's advice.
from net.grinder.script.Grinder import grinder
from net.grinder.script import Test
from java.lang import StringBuilder

builder = Test(1, "StringBuilder.append").wrap(StringBuilder())

class TestRunner:
    def __call__(self):
        builder.append("x")
