/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2023 The JReleaser authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jreleaser.model.internal.announce;

import org.jreleaser.bundle.RB;
import org.jreleaser.model.Active;
import org.jreleaser.model.JReleaserException;
import org.jreleaser.model.internal.JReleaserContext;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Path;
import java.util.Map;

import static java.util.Collections.unmodifiableMap;
import static org.jreleaser.model.Constants.HIDE;
import static org.jreleaser.model.Constants.KEY_TAG_NAME;
import static org.jreleaser.model.Constants.UNSET;
import static org.jreleaser.model.JReleaserOutput.nag;
import static org.jreleaser.model.api.announce.TeamsAnnouncer.TYPE;
import static org.jreleaser.mustache.MustacheUtils.applyTemplate;
import static org.jreleaser.mustache.MustacheUtils.applyTemplates;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.2.0
 */
@Deprecated
public final class TeamsAnnouncer extends AbstractAnnouncer<TeamsAnnouncer, org.jreleaser.model.api.announce.TeamsAnnouncer> {
    private String webhook;
    private String messageTemplate;

    private final org.jreleaser.model.api.announce.TeamsAnnouncer immutable = new org.jreleaser.model.api.announce.TeamsAnnouncer() {
        @Override
        public String getType() {
            return org.jreleaser.model.api.announce.TeamsAnnouncer.TYPE;
        }

        @Override
        public String getWebhook() {
            return webhook;
        }

        @Override
        public String getMessageTemplate() {
            return messageTemplate;
        }

        @Override
        public String getName() {
            return TeamsAnnouncer.this.getName();
        }

        @Override
        public boolean isSnapshotSupported() {
            return TeamsAnnouncer.this.isSnapshotSupported();
        }

        @Override
        public Active getActive() {
            return TeamsAnnouncer.this.getActive();
        }

        @Override
        public boolean isEnabled() {
            return TeamsAnnouncer.this.isEnabled();
        }

        @Override
        public Map<String, Object> asMap(boolean full) {
            return unmodifiableMap(TeamsAnnouncer.this.asMap(full));
        }

        @Override
        public String getPrefix() {
            return TeamsAnnouncer.this.getPrefix();
        }

        @Override
        public Map<String, Object> getExtraProperties() {
            return unmodifiableMap(TeamsAnnouncer.this.getExtraProperties());
        }

        @Override
        public Integer getConnectTimeout() {
            return TeamsAnnouncer.this.getConnectTimeout();
        }

        @Override
        public Integer getReadTimeout() {
            return TeamsAnnouncer.this.getReadTimeout();
        }
    };

    public TeamsAnnouncer() {
        super(TYPE);
    }

    @Override
    public org.jreleaser.model.api.announce.TeamsAnnouncer asImmutable() {
        return immutable;
    }

    @Override
    public void merge(TeamsAnnouncer source) {
        super.merge(source);
        this.webhook = merge(this.webhook, source.webhook);
        this.messageTemplate = merge(this.messageTemplate, source.messageTemplate);

        if (isSet()) {
            nag("announce." + getName() + " is deprecated since 1.4.0 and will be removed in 2.0.0. Use announce.webhooks instead");
        }
    }

    @Override
    protected boolean isSet() {
        return super.isSet() ||
            isNotBlank(webhook) ||
            isNotBlank(messageTemplate);
    }

    public String getResolvedMessageTemplate(JReleaserContext context, Map<String, Object> extraProps) {
        Map<String, Object> props = context.fullProps();
        applyTemplates(props, getResolvedExtraProperties());
        props.put(KEY_TAG_NAME, context.getModel().getRelease().getReleaser()
            .getEffectiveTagName(context.getModel()));
        props.putAll(extraProps);

        Path templatePath = context.getBasedir().resolve(messageTemplate);
        try {
            Reader reader = java.nio.file.Files.newBufferedReader(templatePath);
            return applyTemplate(reader, props);
        } catch (IOException e) {
            throw new JReleaserException(RB.$("ERROR_unexpected_error_reading_template",
                context.relativizeToBasedir(templatePath)));
        }
    }

    public String getWebhook() {
        return webhook;
    }

    public void setWebhook(String webhook) {
        this.webhook = webhook;
    }

    public String getMessageTemplate() {
        return messageTemplate;
    }

    public void setMessageTemplate(String messageTemplate) {
        this.messageTemplate = messageTemplate;
    }

    @Override
    protected void asMap(boolean full, Map<String, Object> props) {
        props.put("webhook", isNotBlank(webhook) ? HIDE : UNSET);
        props.put("messageTemplate", messageTemplate);
    }

    public WebhookAnnouncer asWebhookAnnouncer() {
        WebhookAnnouncer announcer = new WebhookAnnouncer();
        announcer.setName(getName());
        announcer.setWebhook(webhook);
        announcer.setMessageTemplate(messageTemplate);
        announcer.setStructuredMessage(false);
        announcer.setConnectTimeout(getConnectTimeout());
        announcer.setReadTimeout(getReadTimeout());
        announcer.setExtraProperties(getExtraProperties());
        return announcer;
    }
}
