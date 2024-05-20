为了方便使用者上传文件和持续部署，此插件提供了一个配套的 CLI 工具，用法如下：

```bash
❯ npx halo-static-pages-deploy-cli deploy --help
Usage: halo-static-pages-deploy deploy [options]

Deploy static pages

Options:
  -f, --file <path>        Specify an input file
  -e, --endpoint <string>  Halo API endpoint
  -i, --id <string>        Static Page ID
  -t, --token <string>     Personal access token
  -h, --help               display help for command
```

示例：

```bash
npx halo-static-pages-deploy-cli deploy -e https://demo.halo.run -i project-FRGuW -t pat_abcd -f ./dist
```

此外，你也可以通过 GitHub Actions 和此 CLI 实现自动部署，GitHub Actions 示例：

```yaml
name: Build and Deploy

on:
  push:
    branches: [main]

jobs:
  deploy:
    runs-on: ubuntu-latest
    steps:
      - name: Checkout
        uses: actions/checkout@v4
      - uses: pnpm/action-setup@v3
        with:
          version: 8
      - uses: actions/setup-node@v4
        with:
          node-version: '20'
          cache: 'pnpm'
      - name: Install dependencies
        run: pnpm install
      - name: Build
        run: pnpm build
      - name: Deploy to Halo
        run: |
          npx halo-static-pages-deploy-cli deploy -e ${{ secrets.ENDPOINT }} -i ${{ secrets.ID }} -t ${{ secrets.PAT }} -f dist
```

需要注意，你需要在仓库的 Secrets 中添加 `ENDPOINT`、`ID` 和 `PAT` 三个变量，分别对应 Halo 网站地址、项目 ID 和个人令牌（需要勾选 **静态网页项目 -> 项目资源上传** 权限），**请不要将这些敏感信息暴露在公开仓库中**。
