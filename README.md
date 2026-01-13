# Crypto Stripper

> [!NOTE]  
> This is a work in progress, playground, full documentation and wiki will be available soon.

`Crypto Stripper` is a `Burp Suite` extension designed to facilitate the penetration testing of applications that implement encryption for HTTP requests and responses.

It is intended as a general-purpose solution, capable of operating across a broad range of encryption implementations, rather than being constrained to specific schemes or narrowly defined use cases. For example:


- [Symmetric encryption with randomly generated keys](https://github.com/dfuribez/crypto-stripper/wiki/Bypassing-symmetric-encryption).
- [Asymmetric encryption](https://github.com/dfuribez/crypto-stripper/wiki/Bypassing-asymmetric-encryption), regardless of whether the keys are hardcoded or dynamically generated and exchanged via separate requests.
- [Bypassing signature verification mechanisms](https://github.com/dfuribez/crypto-stripper/wiki/Bypassing-signature-verification)
- Enables [seamless integration with external tools](https://github.com/dfuribez/crypto-stripper/wiki/Using-automatic-and-external-tools) such as SQLMap, ffuf, and similar automated testing frameworks.
- All of the above, and more.

The extension does not perform any encryption or decryption on its own; instead, it relies on a pentester-provided script that implements the required transformation logic. The script receives the complete request or response and returns the transformed output.

```mermaid
---
config:
  layout: elk
---
flowchart TB
    n1["Request<br>Response"] --> n5["Burp Suite with<br>Crypto Stripper"]
    n5 --> n6@{ label: "<p data-start=\"264\" data-end=\"295\">Endpoint within Crypto Stripper scope?*</p><br><br><p data-start=\"297\" data-end=\"306\"></p>" }
    n6 -- Yes --> n7["Transformed with the custom scripts"]
    n7 --> n9["Display to the pentester"]
    n6 -- No --> n9
    n9 --> n10["Server<br>Client"]

    n1@{ shape: rounded}
    n6@{ shape: decision}
    n9@{ shape: proc}
    n10@{ shape: rounded}
```

>[!IMPORTANT]
>\* To allow for even more granularity, Crypto Stripper is not triggered on every request or response, even if it falls within the Burp Suite scope. In real-world scenarios, not all application requests are encrypted, nor are they necessarily encrypted or signed using the same method across all endpoints. <br> <br> To address this, Crypto Stripper implements its own scoping mechanism. This scope can be defined on a per-endpoint basis or through the use of regular expressions. [Read more](https://github.com/dfuribez/crypto-stripper/wiki/Configuration#adding-endpoints-to-the-scope)


# How to start?
1. Download and install the [latest available release](https://github.com/dfuribez/crypto-stripper/releases).
2. [Set up the core extension settings](https://github.com/dfuribez/crypto-stripper/wiki/Configuration).
3. [Write your response/request scripts.](https://github.com/dfuribez/crypto-stripper/wiki/Stripper-scripts)
4. [Include the target endpoint in the Stripper scope.](https://github.com/dfuribez/crypto-stripper/wiki/Configuration#adding-endpoints-to-the-scope)
5. [Test that the scripts work as intended.](https://github.com/dfuribez/crypto-stripper/wiki/Debugging)
6. Have fun ;-)


# Examples
To understand a little better how to play with the extension, a playground is available

- [Bypassing asymmetric encryption](https://github.com/dfuribez/crypto-stripper/wiki/Bypassing-asymmetric-encryption)
- [Bypassing signature verification](https://github.com/dfuribez/crypto-stripper/wiki/Bypassing-signature-verification)
- [Bypassing symmetric encryption](https://github.com/dfuribez/crypto-stripper/wiki/Bypassing-symmetric-encryption)
- [Using automatic and external tools](https://github.com/dfuribez/crypto-stripper/wiki/Using-automatic-and-external-tools)
- [Bypassing client-side validations](https://github.com/dfuribez/crypto-stripper/wiki/Bypassing-client-side-validations)


# Other uses

## Request Highlighting
Crypto Stripper can also be used along with the Firefox extension ProxyContain [[Firefox]](https://addons.mozilla.org/en-US/firefox/addon/proxycontain/) [[Source code]](https://github.com/dfuribez/proxycontain) to highlight the requests coming from a specific container.

<p align="center">
<img src="https://github.com/user-attachments/assets/29db1865-731a-48db-8fa4-ee12a47b5795" />
</p>

Each color represents a request incoming from a specific container, making it easier to pentest multi-session applications.


## Insert payload

The Insert Payload feature provides a window that allows users to input either a file or a string of a specified length—randomized or fixed. The resulting output can be encoded or left in plain form. To open the window, right-click where you want to insert the payload and navigate to Extensions → Crypto Stripper → Insert Payload.

<p align="center">
<img src="https://github.com/user-attachments/assets/086ee62f-bf17-4014-b9e4-cc3eebdac566" />
</p>
