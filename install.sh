#!/bin/bash
set -u

# string formatters
if [[ -t 1 ]]; then
  tty_escape() { printf "\033[%sm" "$1"; }
else
  tty_escape() { :; }
fi
tty_mkbold() { tty_escape "1;$1"; }
tty_underline="$(tty_escape "4;39")"
tty_blue="$(tty_mkbold 34)"
tty_red="$(tty_mkbold 31)"
tty_bold="$(tty_mkbold 39)"
tty_reset="$(tty_escape 0)"

shell_join() {
  local arg
  printf "%s" "$1"
  shift
  for arg in "$@"; do
    printf " "
    printf "%s" "${arg// /\ }"
  done
}

ohai() {
  printf "${tty_blue}==>${tty_bold} %s${tty_reset}\n" "$(shell_join "$@")"
}

abort() {
  printf "%s\n" "$1"
  exit 1
}

execute() {
  if ! "$@"; then
    abort "$(printf "Failed during: %s" "$(shell_join "$@")")"
  fi
}

getc() {
  local save_state
  save_state=$(/bin/stty -g)
  /bin/stty raw -echo
  IFS= read -r -n 1 -d '' "$@"
  /bin/stty "$save_state"
}

wait_for_user() {
  local c
  echo
  echo "Press RETURN to continue or any other key to abort"
  getc c
  # we test for \r and \n because some stuff does \r instead
  if ! [[ "$c" == $'\r' || "$c" == $'\n' ]]; then
    exit 1
  fi
}

ohai "This script will install:"
echo "${HOME}/.gradle/init.d/nokee.init.gradle"

directories=(.gradle/init.d)
mkdirs=()
for dir in "${directories[@]}"; do
  if ! [[ -d "${HOME}/${dir}" ]]; then
    mkdirs+=("${HOME}/${dir}")
  fi
done

if [[ "${#mkdirs[@]}" -gt 0 ]]; then
  ohai "The following new directories will be created:"
  printf "%s\n" "${mkdirs[@]}"
fi

if [[ -t 0 && -z "${CI-}" ]]; then
  wait_for_user
fi

if [[ "${#mkdirs[@]}" -gt 0 ]]; then
  execute "/bin/mkdir" "-p" "${mkdirs[@]}"
fi

ohai "Downloading and installing Nokee..."
(
  execute "curl" "-o" "${HOME}/.gradle/init.d/nokee.init.gradle" "https://raw.githubusercontent.com/nokeedev/gradle-native/master/nokee.init.gradle"
) || exit 1

ohai "Installation successful!"
echo

# Use the shell's audible bell.
if [[ -t 1 ]]; then
  printf "\a"
fi

