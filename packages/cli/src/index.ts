#!/usr/bin/env node

import { Command } from "commander";
import cliProgress from "cli-progress";
import { version } from "../package.json";
import axios, { AxiosProgressEvent } from "axios";
import FormData from "form-data";
import fs from "fs";
import AdmZip from "adm-zip";
import path from "path";
import os from "os";
import { randomUUID } from "crypto";

const program = new Command();

program.name("halo-static-pages-deploy").alias("hsp").version(version);

program
  .command("deploy")
  .description("Deploy static pages")
  .requiredOption("-f, --file <path>", "Specify an input file")
  .requiredOption("-e, --endpoint <string>", "Halo API endpoint")
  .requiredOption("-i, --id <string>", "Static Page ID")
  .requiredOption("-t, --token <string>", "Personal access token")
  .action(async (str) => {
    const fileStat = fs.statSync(str.file);

    let distToUpload = str.file;

    if (fileStat.isDirectory()) {
      const zip = new AdmZip();

      zip.addLocalFolder(str.file);

      const tmpdir = fs.mkdtempSync(path.join(os.tmpdir(), "halo-static-pages-deploy-"));

      const zipDir = path.join(tmpdir, `${randomUUID()}.zip`);

      zip.writeZip(zipDir);

      distToUpload = zipDir;
    }

    const formData = new FormData();
    formData.append("file", fs.createReadStream(distToUpload));
    formData.append("unzip", fileStat.isDirectory() ? "true" : "false");

    const processBar = new cliProgress.SingleBar(
      {
        format: "Uploading [{bar}] {percentage}% | ETA: {eta}s | {value}/{total}",
      },
      cliProgress.Presets.legacy
    );

    processBar.start(100, 0);

    await axios.post(
      `${str.endpoint}/apis/console.api.staticpage.halo.run/v1alpha1/projects/${str.id}/upload`,
      formData,
      {
        headers: {
          Authorization: `Bearer ${str.token}`,
        },
        onUploadProgress: (progressEvent: AxiosProgressEvent) => {
          const process = parseInt(Math.round((progressEvent.loaded * 100) / (progressEvent.total || 1)) + "");
          processBar.update(process);
        },
      }
    );

    processBar.stop();

    console.log("Deployed successfully");
  });

program.parse(process.argv);
