#/bin/bash

# Save location and jump to root
oldloc=$(pwd)
cd "$( dirname "${BASH_SOURCE[0]}" )"

# Create Build Target
echo "==== Creating ./bin ===="
rm -rf ./bin
mkdir -p ./bin/

# Build Code
echo "==== Building Source ===="
javac -d ./bin/ ./src/*.java

# Copy Non-code assets
echo "==== Copying Assets ===="
cp -r ./assets/* ./bin/

# Package
echo "==== Packaging (jar) ===="
cd ./bin/
jar -cvfm ../AutoArc.jar ../AutoArc.manifest *
cd ../

# Return to last location
cd ${oldloc}

echo "**** DONE ****"
