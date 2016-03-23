# sistedes.biblioteca
Tools for the Sistedes digital library.

Generates a list of handle value lines to register the handles for the wordpress entries in the [Sistedes Digital Library](http://biblioteca.sistedes.es).

## Command line options:

usage: java -jar <this-file.jar> -p <prefix> [-i <input file>] [-o <output file>] [-g] [-d]
 -p,--prefix <prefix>        Handle server's prefix (mandatory)
 -i,--input <input file>     The input file (optional, stdin will be used if no input file is specified)
 -o,--output <output file>   The output file (optional, stdout will be used if no input file is specified)
 -g,--guid                   Use the guid tag instead of the link
 -d,--add-delete             Add delete statements before the creation

