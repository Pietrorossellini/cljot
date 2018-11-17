#!/bin/bash
CLOVERAGE_VERSION=1.0.13 lein with-profile test cloverage --codecov
bash <(curl -s https://codecov.io/bash)
