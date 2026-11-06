# Perf probe: one HTTP GET per run against a local server on port 7001, such
# as `python3 -m http.server -p HTTP/1.1 7001`. Unlike examples/http.py it
# writes nothing to disk, so the number measures the HTTP plugin.
from net.grinder.script import Test
from net.grinder.plugin.http import HTTPRequest

request = HTTPRequest()
Test(1, "GET /").record(request)

class TestRunner:
    def __call__(self):
        request.GET("http://localhost:7001/")
