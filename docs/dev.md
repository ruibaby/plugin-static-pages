# 用于在 Halo 轻松 host 静态页面

## 后端基础设计

可以利用 Halo 本身的 static 目录映射，此插件的主要工作为操作 static 目录，可以在 static 目录创建新的文件夹并管理。

模型：

```yaml
kind: Project
group: staticpage.halo.run
version: v1alpha1
metadata:
    name: my-blog
spec:
    # static/my-blog
    directory: my-blog
    title: My Blog
    description: My Blog Description
    rewrites:
        -   source: "**"
            target: index.html
        -   source: "/"
            target: index.html
```

接口：

- POST /apis/console.api.staticpage.halo.run/v1alpha1/projects/{name}/upload 上传静态资源，利用 metadata.name 和
  secret 进行认证。
- DELETE /apis/console.api.staticpage.halo.run/v1alpha1/projects/{name}/files?path=xxx 删除项目中的文件或目录。
- GET /apis/console.api.staticpage.halo.run/v1alpha1/projects/{name}/files?path=xxx 获取项目文件列表。
- GET /apis/console.api.halo.run/v1alpha1/projects/{name}/file-content?path=xxx 读取静态资源文件内容。
- PUT /apis/console.api.staticpage.halo.run/v1alpha1/projects/{name}/file-content?path=xxx.index,
  body: `{ "content": "hello" }` 更新静态资源文件内容。

## CLI

用法：

```yaml
hsp deploy --name=my-blog --secret=my-secret --dist=dist
```

## GitHub Action

用于持续部署。

```yaml
name: Node.js CI

on:
    push:
        branches: [ main ]
    # 或者 release 的时候部署
    release:
        types:
            - created
jobs:
    build:
        runs-on: ubuntu-latest
        needs: check
        if: github.event_name == 'push'
        steps:
            -   uses: actions/checkout@v2

            -   name: Install pnpm
                uses: pnpm/action-setup@v2.0.1
                with:
                    version: 8

            -   name: Use Node.js 18.x
                uses: actions/setup-node@v2
                with:
                    node-version: 18.x
                    cache: 'pnpm'

            -   run: pnpm install
            -   run: pnpm build

            -   name: Deploy
                uses: ruibaby/static-pages-deploy@main
                env:
                    NAME: ${{ secrets.WEBSITE_ID }}
                    SECRET: ${{ secrets.WEBSITE_SECRET }}
                    HOST: ${{ secrets.HOST }}
                    TARGET: "dist/"
```
