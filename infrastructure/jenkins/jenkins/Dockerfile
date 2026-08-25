FROM jenkins/jenkins:lts


USER root


# =========================
# Основные инструменты
# =========================

RUN apt-get update && apt-get install -y \
    git \
    maven \
    docker.io \
    python3 \
    python3-pip \
    python3-venv \
    curl \
    wget \
    unzip \
    gnupg \
    ca-certificates \
    && rm -rf /var/lib/apt/lists/*



# =========================
# Google Chrome
# =========================

RUN wget -q -O - https://dl.google.com/linux/linux_signing_key.pub \
    | gpg --dearmor \
    -o /usr/share/keyrings/google-chrome.gpg


RUN echo "deb [arch=amd64 signed-by=/usr/share/keyrings/google-chrome.gpg] \
http://dl.google.com/linux/chrome/deb/ stable main" \
> /etc/apt/sources.list.d/google-chrome.list


RUN apt-get update && \
    apt-get install -y google-chrome-stable && \
    rm -rf /var/lib/apt/lists/*



# =========================
# ChromeDriver
# =========================

RUN DRIVER_VERSION=$(curl -s https://googlechromelabs.github.io/chrome-for-testing/LATEST_RELEASE_STABLE) && \
    wget https://storage.googleapis.com/chrome-for-testing-public/${DRIVER_VERSION}/linux64/chromedriver-linux64.zip && \
    unzip chromedriver-linux64.zip && \
    mv chromedriver-linux64/chromedriver /usr/local/bin/chromedriver && \
    chmod +x /usr/local/bin/chromedriver && \
    rm -rf chromedriver-linux64.zip chromedriver-linux64



# =========================
# Allure CLI
# =========================

RUN curl -o allure.tgz -OL \
https://github.com/allure-framework/allure2/releases/download/2.29.0/allure-2.29.0.tgz && \
tar -zxvf allure.tgz -C /opt && \
ln -s /opt/allure-2.29.0/bin/allure /usr/bin/allure && \
rm allure.tgz



# =========================
# Jenkins plugins
# =========================

COPY plugins.txt /usr/share/jenkins/ref/plugins.txt


RUN jenkins-plugin-cli \
    --plugin-file /usr/share/jenkins/ref/plugins.txt



USER jenkins
