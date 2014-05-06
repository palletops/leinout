# leinout

A library for doing things with git, github, travis and lein, from
within a lein plugin.

## Usage

### Lein

The `com.palletops.leinout.lein` namespace contains functions for
interacting executing lein tasks.

### Git

The `com.palletops.leinout.git` namespace contains functions for
interacting executing git tasks.

### Git-Flow

The `com.palletops.leinout.git-flow` namespace contains functions for
interacting executing git-flow related tasks.

### Github

The `com.palletops.leinout.github` namespace contains functions for
interacting executing git-flow related tasks.

### Travis

The `com.palletops.leinout.travis` namespace contains functions for
interacting with travis via the travis api.

```clj
(require '[com.palletops.leinout.travis :refer :all])
(history "username" "reponame")
```

## License

Copyright © 2014 Hugo Duncan

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
