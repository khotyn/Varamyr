## Introduction

Nook Simple Touch does not support Chinese perfectly. You should add a piece of css code into the epub file to make it readable in Nook Simple Touch.

Calibre is a fine tool to do this, but it's UI just sucks.I just need a command line tool to do it simply and quickly.

So here comes Varamyr (Varamyr is a wilding warg in George.R.Martin's famous book **A Song of Ice and Fire**).

## How to use it?

### Requirement:

1. You should have a JRE environment to use this tool.
2. You should use this tool under an unix-like system. You may get some trouble if you use it in Microsoft Windows.

### Use

1. Download the jar file from [here](https://github.com/downloads/khotyn/Varamyr/varamyr.jar).
2. Open a terminal.
3. Go to the directory where you put the jar file you just downloaded.
3. type `java -jar varamyr.jar <path-to-the-epub-file>`
4. You should see a new epub file named `xxxx-varamyr.epub` is generated in the same directory of the epub file you just specified.
