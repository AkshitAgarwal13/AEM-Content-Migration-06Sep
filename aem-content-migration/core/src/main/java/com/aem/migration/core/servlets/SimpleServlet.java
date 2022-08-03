/*
 *  Copyright 2015 Adobe Systems Incorporated
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.aem.migration.core.servlets;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.servlet.Servlet;

import org.apache.commons.io.IOUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.apache.sling.jcr.contentloader.ContentImportListener;
import org.apache.sling.jcr.contentloader.ContentImporter;
import org.apache.sling.jcr.contentloader.ImportOptions;
import org.apache.sling.servlets.annotations.SlingServletResourceTypes;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.propertytypes.ServiceDescription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

// TODO: Auto-generated Javadoc
/**
 * Servlet that writes some sample content into the response. It is mounted for
 * all resources of a specific Sling resource type. The
 * {@link SlingSafeMethodsServlet} shall be used for HTTP methods that are
 * idempotent. For write operations use the {@link SlingAllMethodsServlet}.
 */
@Component(service = { Servlet.class })
@SlingServletResourceTypes(
        resourceTypes="migration/components/page",
        methods=HttpConstants.METHOD_GET,
        extensions="txt")
@ServiceDescription("Simple Demo Servlet")
public class SimpleServlet extends SlingSafeMethodsServlet {
	
	/** The Constant log. */
	private static final Logger log = LoggerFactory.getLogger(ContentProcessorServlet.class);

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The content importer. */
    @Reference
    private ContentImporter contentImporter;

    /**
     * Do get.
     *
     * @param request the request
     * @param response the response
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Override
    protected void doGet(final SlingHttpServletRequest request, final SlingHttpServletResponse response)
            throws
            IOException {

        final MyContentImportListener contentImportListener = new MyContentImportListener();
        final Node node = request.getResource().adaptTo(Node.class);

        if (node != null) {

            final MyImportOptions importOptions = new MyImportOptions();
            try (InputStream inputStream = IOUtils.toInputStream("{\"foo\":\"bar\"}", StandardCharsets.UTF_8)) {
                this.contentImporter.importContent(node, "my-imported-structure", "application/json", inputStream, importOptions, contentImportListener);
            } catch (final RepositoryException e) {
                log.error(e.getMessage(), e);
            }
        }

        response.setContentType("text/plain");
        response.getWriter().println(contentImportListener);

    }
    
    /**
     * The Class MyImportOptions.
     */
    @Getter
    @Builder
    private static final class MyImportOptions extends ImportOptions {

        /** The checkin. */
        private final boolean checkin = false;
        
        /** The auto checkout. */
        private final boolean autoCheckout = false;
        
        /** The overwrite. */
        private final boolean overwrite = true;
        
        /** The property overwrite. */
        private final boolean propertyOverwrite = true;

        /**
         * Checks if is ignored import provider.
         *
         * @param extension the extension
         * @return true, if is ignored import provider
         */
        @Override
        public boolean isIgnoredImportProvider(final String extension) { return false; }

		/**
		 * Checks if is checkin.
		 *
		 * @return true, if is checkin
		 */
		@Override
		public boolean isCheckin() {
			// TODO Auto-generated method stub
			return checkin;
		}

		/**
		 * Checks if is overwrite.
		 *
		 * @return true, if is overwrite
		 */
		@Override
		public boolean isOverwrite() {
			// TODO Auto-generated method stub
			return overwrite;
		}

		/**
		 * Checks if is property overwrite.
		 *
		 * @return true, if is property overwrite
		 */
		@Override
		public boolean isPropertyOverwrite() {
			// TODO Auto-generated method stub
			return propertyOverwrite;
		}

		
    }

    /**
     * The listener interface for receiving myContentImport events.
     * The class that is interested in processing a myContentImport
     * event implements this interface, and the object created
     * with that class is registered with a component using the
     * component's <code>addMyContentImportListener<code> method. When
     * the myContentImport event occurs, that object's appropriate
     * method is invoked.
     *
     * @see MyContentImportEvent
     */
    @Getter
    @ToString
    private static final class MyContentImportListener implements ContentImportListener {

        /** The changes. */
        private final com.google.common.collect.Multimap<String, String> changes =
                com.google.common.collect.ArrayListMultimap.create();

        /**
         * On reorder.
         *
         * @param orderedPath the ordered path
         * @param beforeSibbling the before sibbling
         */
        @Override
        public void onReorder(final String orderedPath, final String beforeSibbling) {this.changes.put("onReorder", String.format("%s, %s", orderedPath, beforeSibbling)); }

        /**
         * On move.
         *
         * @param srcPath the src path
         * @param destPath the dest path
         */
        @Override
        public void onMove(final String srcPath, final String destPath) { this.changes.put("onMove", String.format("%s, %s", srcPath, destPath)); }

        /**
         * On modify.
         *
         * @param srcPath the src path
         */
        @Override
        public void onModify(final String srcPath) { this.changes.put("onModify", srcPath); }

        /**
         * On delete.
         *
         * @param srcPath the src path
         */
        @Override
        public void onDelete(final String srcPath) { this.changes.put("onDelete", srcPath); }

        /**
         * On create.
         *
         * @param srcPath the src path
         */
        @Override
        public void onCreate(final String srcPath) { this.changes.put("onCreate", srcPath); }

        /**
         * On copy.
         *
         * @param srcPath the src path
         * @param destPath the dest path
         */
        @Override
        public void onCopy(final String srcPath, final String destPath) { this.changes.put("onCopy", String.format("%s, %s", srcPath, destPath)); }

        /**
         * On checkin.
         *
         * @param srcPath the src path
         */
        @Override
        public void onCheckin(final String srcPath) { this.changes.put("onCheckin", srcPath); }

        /**
         * On checkout.
         *
         * @param srcPath the src path
         */
        @Override
        public void onCheckout(final String srcPath) { this.changes.put("onCheckout", srcPath); }
    }
}