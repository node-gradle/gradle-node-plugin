{
  description = "A Nix-flake-based development environment";

  inputs.nixpkgs.url = "github:NixOS/nixpkgs/release-23.05";

  outputs = { self, nixpkgs }:
    let
      javaVersion = 11;
      nodeVersion = 18;
      overlays = [
        (final: prev: rec {
          jdk = prev."jdk${toString javaVersion}";
          nodejs = prev."nodejs-${toString nodeVersion}_x";
          pnpm = prev.nodePackages.pnpm;
        })
      ];
      supportedSystems = [ "x86_64-linux" "aarch64-linux" "x86_64-darwin" "aarch64-darwin" ];
      forEachSupportedSystem = f: nixpkgs.lib.genAttrs supportedSystems (system: f {
        pkgs = import nixpkgs { inherit overlays system; };
      });
    in
    {
      devShells = forEachSupportedSystem ({ pkgs }: {
        default = pkgs.mkShell {
          NIX_LD_LIBRARY_PATH = nixpkgs.lib.makeLibraryPath [
            pkgs.stdenv.cc.cc.lib
          ];
          NIX_LD = nixpkgs.lib.fileContents "${pkgs.stdenv.cc}/nix-support/dynamic-linker";
          packages = with pkgs; [ jdk nodejs pnpm stdenv ];
        };
      });
    };
}
