---
title: kota-app Privacy Policy
date: 2026-10-03T12:00:00+09:00
tags:
unlisted: true
---

This privacy policy describes how kota-app handles Google user data. kota-app is a Discord bot operated by Takuma Homma (mataku) for personal use only. It is not offered to any other users.

"I" in this privacy policy document refers to Takuma Homma.

- Effective date: October 3, 2026
- Last updated: October 3, 2026

## Google User Data Accessed

kota-app requests only the `https://www.googleapis.com/auth/drive.file` scope. With this scope, kota-app can access only the files and folders in my Google Drive that kota-app itself has created. kota-app does not read, list, or modify any other files in Google Drive.

## How Google User Data Is Used

kota-app uses Google Drive access solely to create and update invoice files (xlsx) in my own Google Drive, in response to commands I run in Discord.

## Data Handled

The invoice files contain the following information, all of which is my own:

- My working hours
- The billing recipient
- My address, phone number, and email address (as required on invoices)

## Data Storage

- OAuth tokens are stored as secrets in Cloudflare Workers.
- Invoice data is stored in Cloudflare (D1, KV, and R2) and in my own Google Drive.

## Data Sharing

kota-app does not share, sell, or transfer Google user data to any third party. Google user data is not used for advertising.

## Limited Use

kota-app's use and transfer of information received from Google APIs will adhere to the [Google API Services User Data Policy](https://developers.google.com/terms/api-services-user-data-policy), including the Limited Use requirements.

## Revoking Access

You can revoke kota-app's access to your Google account at any time from [https://myaccount.google.com/permissions](https://myaccount.google.com/permissions).

## Contact Me

If you have any questions about this privacy policy, contact me at sfprhythnn at gmail.com.
