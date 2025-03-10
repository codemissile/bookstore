# Bookstore Application Deployment on AWS EKS

This guide provides step-by-step instructions to deploy the Bookstore application on AWS Elastic Kubernetes Service (EKS). It covers building and pushing Docker images, setting up EKS, and deploying Kubernetes manifests.

## Prerequisites
Before proceeding, ensure you have the following installed:
- AWS CLI
- kubectl
- eksctl
- Docker
- Kubernetes
- Terraform (if managing infrastructure as code)

## Step 1: Authenticate AWS CLI
```sh
aws configure
```
Set up your AWS credentials with your access key and secret key.

## Step 2: Create an EKS Cluster
```sh
eksctl create cluster --name bookstore-cluster --region <your-region> --nodegroup-name standard-workers --node-type t3.small --nodes 2 --nodes-min 2 --nodes-max 4
```
This command creates an EKS cluster with a node group of `t3.small` instances.

## Step 3: Authenticate with EKS Cluster
```sh
aws eks --region <your-region> update-kubeconfig --name bookstore-cluster
```
Verify the connection:
```sh
kubectl get nodes
```

## Step 4: Create an Amazon ECR Repository
```sh
aws ecr create-repository --repository-name bookstore/cart-service
aws ecr create-repository --repository-name bookstore/catalog-service
aws ecr create-repository --repository-name bookstore/frontend
```
Retrieve login credentials and authenticate Docker:
```sh
eval $(aws ecr get-login-password --region <your-region> | docker login --username AWS --password-stdin <aws-account-id>.dkr.ecr.<your-region>.amazonaws.com)
```

## Step 5: Build and Push Docker Images
Navigate to each service directory and run:
```sh
docker build -t <aws-account-id>.dkr.ecr.<your-region>.amazonaws.com/bookstore/cart-service:latest .
docker push <aws-account-id>.dkr.ecr.<your-region>.amazonaws.com/bookstore/cart-service:latest
```
Repeat for the `catalog-service` and `frontend`:
```sh
docker build -t <aws-account-id>.dkr.ecr.<your-region>.amazonaws.com/bookstore/catalog-service:latest .
docker push <aws-account-id>.dkr.ecr.<your-region>.amazonaws.com/bookstore/catalog-service:latest

docker build -t <aws-account-id>.dkr.ecr.<your-region>.amazonaws.com/bookstore/frontend:latest .
docker push <aws-account-id>.dkr.ecr.<your-region>.amazonaws.com/bookstore/frontend:latest
```

## Step 6: Deploy PostgreSQL
Apply the Kubernetes manifests for PostgreSQL:
```sh
kubectl apply -f k8s/postgres/postgres-secret.yaml
kubectl apply -f k8s/postgres/storageclass.yaml
kubectl apply -f k8s/postgres/postgres.yaml
```
Verify the deployment:
```sh
kubectl get pods -n default
```

## Step 7: Deploy Backend Services
Deploy the backend services:
```sh
kubectl apply -f k8s/services/cart-service.yaml
kubectl apply -f k8s/services/catalog-service.yaml
```
Check that the services are running:
```sh
kubectl get pods
kubectl get svc
```

## Step 8: Deploy Frontend
Deploy the frontend service:
```sh
kubectl apply -f k8s/services/frontend-deployment.yaml
kubectl apply -f k8s/services/frontend-service.yaml
```

## Step 9: Verify Application Deployment
Get the external load balancer URL:
```sh
kubectl get svc frontend-service
```
Access the application via the displayed external URL.

## Step 10: Cleanup Resources
To delete the cluster and free up resources:
```sh
eksctl delete cluster --name bookstore-cluster --region <your-region>
```
To delete the ECR repositories:
```sh
aws ecr delete-repository --repository-name bookstore/cart-service --force
aws ecr delete-repository --repository-name bookstore/catalog-service --force
aws ecr delete-repository --repository-name bookstore/frontend --force
```

## Conclusion
You have successfully deployed the Bookstore application on AWS EKS. The application is now running in a scalable Kubernetes environment.

