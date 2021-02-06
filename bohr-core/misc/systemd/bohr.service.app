[Unit]
Description=Bohr
After=network.target

[Service]
User=username
Group=groupname
Type=simple
Restart=on-failure

# For Mainnet node
ExecStart=/home/username/bohr/bohr-cli.sh

# For Testnet node
# ExecStart=/home/username/bohr/bohr-cli.sh --network testnet

# Bohr_WALLET_PASSWORD environment variable is required to automatically unlock your wallet.data file.
# Please ensure sure that the access permission of this service unit file is properly configured when you put your password here.
Environment=Bohr_WALLET_PASSWORD=YOUR_WALLET_PASSWORD

[Install]
WantedBy=multi-user.target
