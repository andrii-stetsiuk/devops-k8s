## kubeplugin (kubectl plugin) — CPU/Memory usage for pods/nodes

Outputs CSV lines: `Resource, Namespace, Name, CPU, Memory`

Requirements:
- `kubectl` in PATH
- `metrics-server` installed in the cluster (for `kubectl top`)

Install:
```bash
chmod +x scripts/kubeplugin
# Install as kubectl plugin (ensure the target directory is on your PATH):
ln -sf "$(pwd)/scripts/kubeplugin" "${HOME}/.local/bin/kubectl-kubeplugin"
# or: sudo ln -sf "$(pwd)/scripts/kubeplugin" /usr/local/bin/kubectl-kubeplugin
```

Usage:
```bash
# Pods (namespace-aware)
kubectl kubeplugin pods -n kube-system
kubectl kubeplugin pods -l app=my-app           # current context namespace if -n omitted

# Nodes (cluster-wide)
kubectl kubeplugin nodes
```

Examples:
```bash
kubectl kubeplugin pods -n kube-system
# Resource, Namespace, Name, CPU, Memory
# pod, kube-system, coredns-7db6d8ff4b-abc12, 3m, 12Mi
# pod, kube-system, metrics-server-577d6c764-xyz34, 4m, 20Mi
```


