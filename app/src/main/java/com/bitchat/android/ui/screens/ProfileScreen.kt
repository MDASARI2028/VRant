package com.bitchat.android.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bitchat.android.campus.*
import com.bitchat.android.ui.theme.VRantColors

/**
 * Profile editor screen with VIT-specific fields
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    profile: VITUserProfile,
    onProfileUpdate: (VITUserProfile) -> Unit,
    onVerifyEmail: (String) -> Boolean,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var nickname by remember { mutableStateOf(profile.nickname) }
    var selectedDepartment by remember { mutableStateOf(profile.department) }
    var selectedYear by remember { mutableIntStateOf(profile.year) }
    var regNumber by remember { mutableStateOf(profile.registrationNumber ?: "") }
    var email by remember { mutableStateOf(profile.email ?: "") }
    var bio by remember { mutableStateOf(profile.bio ?: "") }
    var isAnonymous by remember { mutableStateOf(profile.isAnonymous) }
    var showDepartmentPicker by remember { mutableStateOf(false) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var emailVerified by remember { mutableStateOf(profile.isVerified) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(VRantColors.DarkBase)
    ) {
        // Top bar
        TopAppBar(
            title = {
                Text(
                    "Profile",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            },
            actions = {
                TextButton(
                    onClick = {
                        val updated = profile.copy(
                            nickname = nickname.trim().ifEmpty { "anon" },
                            department = selectedDepartment,
                            year = selectedYear,
                            registrationNumber = regNumber.ifEmpty { null },
                            email = email.ifEmpty { null },
                            isVerified = emailVerified,
                            isAnonymous = isAnonymous,
                            bio = bio.ifEmpty { null }
                        )
                        onProfileUpdate(updated)
                        onBack()
                    }
                ) {
                    Text(
                        "Save",
                        color = VRantColors.Maroon500,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = VRantColors.DarkBase,
                titleContentColor = VRantColors.TextPrimary,
                navigationIconContentColor = VRantColors.TextSecondary
            )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Avatar section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(VRantColors.Maroon500, VRantColors.Gold500)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = nickname.take(2).uppercase().ifEmpty { "?" },
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    if (emailVerified) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Verified,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = VRantColors.VerifiedGold
                            )
                            Text(
                                text = "VIT Verified",
                                style = MaterialTheme.typography.labelSmall,
                                color = VRantColors.VerifiedGold
                            )
                        }
                    }
                }
            }

            // Anonymous mode toggle
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = VRantColors.DarkSurface
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Anonymous Mode",
                            style = MaterialTheme.typography.titleSmall,
                            color = VRantColors.TextPrimary
                        )
                        Text(
                            text = "Hide your identity — only nickname visible via mesh",
                            style = MaterialTheme.typography.labelSmall,
                            color = VRantColors.TextMuted
                        )
                    }
                    Switch(
                        checked = isAnonymous,
                        onCheckedChange = { isAnonymous = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = VRantColors.Maroon500,
                            checkedTrackColor = VRantColors.Maroon500.copy(alpha = 0.3f)
                        )
                    )
                }
            }

            // Nickname field
            ProfileField(
                label = "Nickname",
                value = nickname,
                onValueChange = { if (it.length <= 15) nickname = it },
                placeholder = "Choose a nickname",
                leadingIcon = Icons.Default.Person,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
            )

            // Department picker
            ProfileField(
                label = "Department",
                value = if (selectedDepartment == VITDepartment.UNSET) "" else selectedDepartment.fullName,
                onValueChange = {},
                placeholder = "Select your department",
                leadingIcon = Icons.Default.School,
                readOnly = true,
                onClick = { showDepartmentPicker = true }
            )

            // Year picker
            Column {
                Text(
                    text = "Year",
                    style = MaterialTheme.typography.labelMedium,
                    color = VRantColors.TextSecondary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    (1..5).forEach { year ->
                        FilterChip(
                            selected = selectedYear == year,
                            onClick = { selectedYear = if (selectedYear == year) 0 else year },
                            label = {
                                Text(
                                    when (year) {
                                        1 -> "1st"
                                        2 -> "2nd"
                                        3 -> "3rd"
                                        else -> "${year}th"
                                    },
                                    fontSize = 12.sp
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = VRantColors.Maroon500.copy(alpha = 0.2f),
                                selectedLabelColor = VRantColors.Maroon400
                            )
                        )
                    }
                }
            }

            // Registration number
            ProfileField(
                label = "Registration Number",
                value = regNumber,
                onValueChange = { regNumber = it.uppercase() },
                placeholder = "e.g. 21BCE1234",
                leadingIcon = Icons.Default.Badge,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                )
            )

            // VIT Email verification
            Column {
                ProfileField(
                    label = "VIT Email",
                    value = email,
                    onValueChange = {
                        email = it
                        emailError = null
                    },
                    placeholder = "name@vitstudent.ac.in",
                    leadingIcon = Icons.Default.Email,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Done
                    ),
                    isError = emailError != null,
                    supportingText = emailError
                )

                if (!emailVerified && email.isNotEmpty()) {
                    Button(
                        onClick = {
                            if (onVerifyEmail(email)) {
                                emailVerified = true
                                emailError = null
                            } else {
                                emailError = "Invalid VIT email domain"
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = VRantColors.Gold500,
                            contentColor = VRantColors.TextOnGold
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Verified, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Verify VIT Email", fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            // Bio
            ProfileField(
                label = "Bio",
                value = bio,
                onValueChange = { if (it.length <= 100) bio = it },
                placeholder = "Tell others about yourself...",
                leadingIcon = Icons.Default.Info,
                singleLine = false,
                maxLines = 3
            )

            Spacer(modifier = Modifier.height(80.dp))
        }
    }

    // Department picker bottom sheet
    if (showDepartmentPicker) {
        DepartmentPickerSheet(
            selectedDepartment = selectedDepartment,
            onDepartmentSelected = {
                selectedDepartment = it
                showDepartmentPicker = false
            },
            onDismiss = { showDepartmentPicker = false }
        )
    }
}

@Composable
private fun ProfileField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    leadingIcon: androidx.compose.ui.graphics.vector.ImageVector,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    readOnly: Boolean = false,
    singleLine: Boolean = true,
    maxLines: Int = 1,
    isError: Boolean = false,
    supportingText: String? = null,
    onClick: (() -> Unit)? = null
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = VRantColors.TextSecondary,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = {
                Text(placeholder, color = VRantColors.TextMuted)
            },
            leadingIcon = {
                Icon(
                    leadingIcon,
                    contentDescription = null,
                    tint = VRantColors.Maroon400,
                    modifier = Modifier.size(20.dp)
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
            singleLine = singleLine,
            maxLines = maxLines,
            readOnly = readOnly,
            enabled = onClick == null,
            keyboardOptions = keyboardOptions,
            isError = isError,
            supportingText = supportingText?.let {
                { Text(it, color = VRantColors.Error) }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = VRantColors.Maroon500,
                unfocusedBorderColor = VRantColors.DarkBorder,
                focusedTextColor = VRantColors.TextPrimary,
                unfocusedTextColor = VRantColors.TextPrimary,
                cursorColor = VRantColors.Maroon500,
                disabledTextColor = VRantColors.TextPrimary,
                disabledBorderColor = VRantColors.DarkBorder
            ),
            shape = RoundedCornerShape(12.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DepartmentPickerSheet(
    selectedDepartment: VITDepartment,
    onDepartmentSelected: (VITDepartment) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = VRantColors.DarkElevated
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Select Department",
                style = MaterialTheme.typography.titleMedium,
                color = VRantColors.TextPrimary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            VITDepartment.entries
                .filter { it != VITDepartment.UNSET }
                .forEach { dept ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onDepartmentSelected(dept) }
                            .padding(vertical = 12.dp, horizontal = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = dept.shortName,
                                style = MaterialTheme.typography.titleSmall,
                                color = if (dept == selectedDepartment) VRantColors.Maroon400
                                else VRantColors.TextPrimary,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = dept.fullName,
                                style = MaterialTheme.typography.labelSmall,
                                color = VRantColors.TextMuted
                            )
                        }
                        if (dept == selectedDepartment) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                tint = VRantColors.Maroon400,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    if (dept != VITDepartment.entries.last()) {
                        HorizontalDivider(
                            color = VRantColors.DarkBorder.copy(alpha = 0.3f)
                        )
                    }
                }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
