# DEPLOY API 
- Create API configMap
```
kubectl apply -f k8s/configMap.yaml
```
**Caveat**: with configMap value contain &amp; (ampersand), should replace it with \&amp;
- Then check configMap value, make sure all value is valid
```
kubectl describe configMap env
```
- Then check configMap value, make sure all value is valid
