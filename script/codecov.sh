#!/bin/bash
CLOVERAGE_VERSION=1.0.13 lein cloverage --codecov
bash <(curl -s https://codecov.io/bash)