## Example

```yaml
name: Deploy VitePress site to Halo

on:
  push:
    branches: [main]

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - name: Checkout
        uses: actions/checkout@v4
        with:
          fetch-depth: 0
      - name: Setup Node
        uses: actions/setup-node@v4
        with:
          node-version: 20
          cache: npm
      - name: Setup Pages
        uses: actions/configure-pages@v4
      - name: Install dependencies
        run: npm ci 
      - name: Build with VitePress
        run: npm run docs:build

  deploy:
    uses: ruibaby/plugin-static-pages/actions/cd.yaml@main
    secrets:
      endpoint: ${{ secrets.ENDPOINT }}
      id: ${{ secrets.ID }}
      pat: ${{ secrets.PAT }}
    permissions:
      contents: write
    with:
      dist-path: docs/.vitepress/dist
```
